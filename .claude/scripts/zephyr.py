#!/usr/bin/env python3
import argparse
import html
import json
import os
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

BASE_URL = "https://api.zephyrscale.smartbear.com/v2"
PROJECT_KEY = "HAT"
ROOT_FOLDER = "MobileTests"
AUTOMATED_LABEL = "automated"
REPO_ROOT = Path(__file__).resolve().parents[2]
SECRETS_FILE = REPO_ROOT / "secrets.properties"
TEST_ROOT = REPO_ROOT / "src/test/java/com/praheal/mobile"
SENSITIVE_PATTERNS = {
    "mobile number": r"(?<!\d)(?:\+?91[\s-]?)?[6-9]\d{4}[\s-]?\d{5}(?!\d)",
    "email address": r"[\w.+-]+@[\w-]+\.[\w.]+",
    "password value": r"(?i)\b(?:password|pwd|pin|otp)\b\s*[:=]\s*\S+",
    "token": r"\beyJ[\w-]{10,}\.[\w-]{10,}",
}


def token():
    value = os.environ.get("ZEPHYR_API_TOKEN")
    if not value:
        settings = REPO_ROOT / ".claude" / "settings.local.json"
        if settings.exists():
            value = json.loads(settings.read_text()).get("env", {}).get("ZEPHYR_API_TOKEN")
    if not value:
        sys.exit("ZEPHYR_API_TOKEN is not set (env var or .claude/settings.local.json)")
    return value


def request(method, path, params=None, body=None, allow_missing=False):
    url = BASE_URL + path
    if params:
        url += "?" + urllib.parse.urlencode(params)
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method, headers={
        "Authorization": "Bearer " + token(),
        "Content-Type": "application/json",
    })
    try:
        with urllib.request.urlopen(req) as response:
            raw = response.read()
            return json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        if allow_missing and e.code == 404:
            return None
        sys.exit(f"{method} {path} failed: {e.code} {e.read().decode()}")


def paged(path, params):
    start = 0
    while True:
        page = request("GET", path, {**params, "startAt": start, "maxResults": 100})
        yield from page.get("values", [])
        if page.get("isLast", True):
            return
        start += len(page.get("values", []))


def plain(text):
    if not text:
        return ""
    text = re.sub(r"<br\s*/?>", "\n", text, flags=re.I)
    text = re.sub(r"<[^>]+>", "", text)
    return html.unescape(text).strip()


def rich(text):
    return html.escape(text or "").replace("\n", "<br>")


def users_pool_values():
    values = [v for k, v in os.environ.items() if k.startswith("PRAHEAL_") and v]
    if SECRETS_FILE.exists():
        for line in SECRETS_FILE.read_text().splitlines():
            if "=" in line and not line.lstrip().startswith("#"):
                value = line.split("=", 1)[1].strip()
                if value:
                    values.append(value)
    return values


def sensitive_findings(fields):
    findings = []
    secrets = users_pool_values()
    for field, text in fields:
        if not text:
            continue
        for value in secrets:
            if value in text:
                findings.append(f"{field}: contains a UsersPool value")
        for kind, pattern in SENSITIVE_PATTERNS.items():
            if re.search(pattern, text):
                findings.append(f"{field}: contains a {kind}")
    return sorted(set(findings))


def spec_texts(spec):
    fields = [(name, spec.get(name)) for name in ("name", "objective", "precondition")]
    for number, step in enumerate(spec.get("steps", []), start=1):
        fields += [(f"step {number} {name}", step.get(name)) for name in ("description", "testData", "expectedResult")]
    return fields


def ensure_no_sensitive_data(spec):
    findings = sensitive_findings(spec_texts(spec))
    if findings:
        sys.exit("Refusing to write sensitive data to Zephyr. Describe the data instead "
                 "(e.g. 'Patient mobile number', 'Patient password'):\n  - " + "\n  - ".join(findings))


def folders():
    return list(paged("/folders", {"projectKey": PROJECT_KEY, "folderType": "TEST_CASE"}))


def root_folder_id(all_folders):
    for folder in all_folders:
        if folder["name"] == ROOT_FOLDER and folder.get("parentId") is None:
            return folder["id"]
    sys.exit(f"Root folder '{ROOT_FOLDER}' not found in project {PROJECT_KEY}")


def feature_folder_id(feature, create=False):
    all_folders = folders()
    root_id = root_folder_id(all_folders)
    for folder in all_folders:
        if folder.get("parentId") == root_id and folder["name"] == feature:
            return folder["id"]
    if not create:
        sys.exit(f"Folder '{ROOT_FOLDER}/{feature}' not found")
    created = request("POST", "/folders", body={
        "parentId": root_id, "name": feature, "projectKey": PROJECT_KEY, "folderType": "TEST_CASE"})
    return created["id"]


def names_by_id(path):
    return {item["id"]: item["name"] for item in paged(path, {"projectKey": PROJECT_KEY})}


def steps(key):
    items = []
    for step in paged(f"/testcases/{key}/teststeps", {}):
        inline = step.get("inline") or {}
        items.append({
            "description": plain(inline.get("description")),
            "testData": plain(inline.get("testData")),
            "expectedResult": plain(inline.get("expectedResult")),
        })
    return items


def describe(case, statuses, priorities, folder_names):
    described = {
        "key": case["key"],
        "name": case["name"],
        "folder": folder_names.get((case.get("folder") or {}).get("id")),
        "status": statuses.get((case.get("status") or {}).get("id")),
        "priority": priorities.get((case.get("priority") or {}).get("id")),
        "labels": case.get("labels", []),
        "automated": AUTOMATED_LABEL in case.get("labels", []),
        "objective": plain(case.get("objective")),
        "precondition": plain(case.get("precondition")),
        "steps": steps(case["key"]),
    }
    described["sensitiveData"] = sensitive_findings(spec_texts(described))
    return described


def cmd_folders(_):
    all_folders = folders()
    root_id = root_folder_id(all_folders)
    for folder in all_folders:
        if folder.get("parentId") == root_id:
            print(f"{ROOT_FOLDER}/{folder['name']}\t{folder['id']}")


def cmd_list(args):
    folder_id = feature_folder_id(args.feature)
    for case in paged("/testcases", {"projectKey": PROJECT_KEY, "folderId": folder_id}):
        labels = case.get("labels", [])
        if args.not_automated and AUTOMATED_LABEL in labels:
            continue
        print(f"{case['key']}\t{'automated' if AUTOMATED_LABEL in labels else 'manual'}\t{case['name']}")


def cmd_get(args):
    statuses = names_by_id("/statuses")
    priorities = names_by_id("/priorities")
    folder_names = {f["id"]: f["name"] for f in folders()}
    keys = args.keys
    if args.feature:
        folder_id = feature_folder_id(args.feature)
        keys = [c["key"] for c in paged("/testcases", {"projectKey": PROJECT_KEY, "folderId": folder_id})
                if not (args.not_automated and AUTOMATED_LABEL in c.get("labels", []))]
    result = [describe(request("GET", f"/testcases/{key}"), statuses, priorities, folder_names) for key in keys]
    print(json.dumps(result, indent=2, ensure_ascii=False))


def write_steps(key, spec_steps):
    request("POST", f"/testcases/{key}/teststeps", body={"mode": "OVERWRITE", "items": [
        {"inline": {
            "description": rich(step.get("description")),
            "testData": rich(step.get("testData")) or None,
            "expectedResult": rich(step.get("expectedResult")),
        }} for step in spec_steps]})


def cmd_update(args):
    spec = json.loads(Path(args.spec).read_text())
    ensure_no_sensitive_data(spec)
    case = request("GET", f"/testcases/{args.key}")
    for field in ("name", "labels"):
        if field in spec:
            case[field] = spec[field]
    for field in ("objective", "precondition"):
        if field in spec:
            case[field] = rich(spec[field])
    if "priority" in spec:
        priority_ids = {name: id_ for id_, name in names_by_id("/priorities").items()}
        case["priority"] = {"id": priority_ids[spec["priority"]]}
    request("PUT", f"/testcases/{args.key}", body=case)
    if spec.get("steps"):
        write_steps(args.key, spec["steps"])
    print(args.key)


def cmd_create(args):
    spec = json.loads(Path(args.spec).read_text())
    ensure_no_sensitive_data(spec)
    folder_id = feature_folder_id(args.feature, create=True)
    body = {
        "projectKey": PROJECT_KEY,
        "name": spec["name"],
        "folderId": folder_id,
        "objective": rich(spec.get("objective")),
        "precondition": rich(spec.get("precondition")),
        "priorityName": spec.get("priority", "Normal"),
        "statusName": spec.get("status", "Draft"),
        "labels": spec.get("labels", []),
    }
    created = request("POST", "/testcases", body=body)
    key = created["key"]
    if spec.get("steps"):
        write_steps(key, spec["steps"])
    print(key)


def java_string(value):
    return value.replace('\\"', '"').replace("\\\\", "\\")


def automated_tests():
    tests = []
    for path in sorted(TEST_ROOT.rglob("*Tests.java")):
        source = path.read_text(encoding="utf-8")
        if "extends BaseMobileTest" not in source:
            continue
        feature = path.relative_to(TEST_ROOT).parts[0]
        for block in re.split(r"\n\s*@Test\b", source)[1:]:
            method = re.search(r"public void (\w+)\(", block)
            test_id = re.search(r'@PrahealLabels\.TestID\("([^"]*)"\)', block)
            description = re.search(r'@Description\("((?:[^"\\]|\\.)*)"\)', block)
            tests.append({
                "test": f"{path.stem}.{method.group(1) if method else '?'}",
                "feature": feature,
                "key": test_id.group(1) if test_id else None,
                "description": java_string(description.group(1)) if description else None,
            })
    return tests


def cmd_verify_sync(_):
    all_folders = folders()
    root_id = root_folder_id(all_folders)
    feature_by_folder_id = {f["id"]: f["name"] for f in all_folders if f.get("parentId") == root_id}
    problems = []
    cases = {}
    tests = automated_tests()
    for test in tests:
        key = test["key"]
        if not key:
            problems.append(f"{test['test']}: no @PrahealLabels.TestID")
            continue
        if key not in cases:
            cases[key] = request("GET", f"/testcases/{key}", allow_missing=True)
        case = cases[key]
        if case is None:
            problems.append(f"{test['test']}: {key} does not exist in Zephyr")
            continue
        folder = feature_by_folder_id.get((case.get("folder") or {}).get("id"))
        if folder != test["feature"]:
            problems.append(f"{test['test']}: {key} is in folder '{folder or 'outside ' + ROOT_FOLDER}', "
                            f"expected '{ROOT_FOLDER}/{test['feature']}'")
        if test["description"] != case["name"]:
            problems.append(f"{test['test']}: @Description differs from {key} name\n"
                            f"      code:   {test['description']}\n      zephyr: {case['name']}")
    linked = {test["key"] for test in tests}
    for folder_id, feature in feature_by_folder_id.items():
        for case in paged("/testcases", {"projectKey": PROJECT_KEY, "folderId": folder_id}):
            if AUTOMATED_LABEL in case.get("labels", []) and case["key"] not in linked:
                problems.append(f"{case['key']}: labelled '{AUTOMATED_LABEL}' in {ROOT_FOLDER}/{feature} "
                                f"but no test carries this key")
    if problems:
        print("Zephyr sync check failed:")
        for problem in problems:
            print("  - " + problem)
        sys.exit(1)
    print(f"Zephyr sync OK: {len(tests)} tests linked to {len(cases)} cases in {ROOT_FOLDER}")


def cmd_label(args):
    case = request("GET", f"/testcases/{args.key}")
    labels = case.get("labels", [])
    if args.label not in labels:
        case["labels"] = labels + [args.label]
        request("PUT", f"/testcases/{args.key}", body=case)
    print(f"{args.key}\t{', '.join(case['labels'])}")


def main():
    parser = argparse.ArgumentParser(description=f"Zephyr Scale helper for {PROJECT_KEY}/{ROOT_FOLDER}")
    sub = parser.add_subparsers(dest="command", required=True)

    sub.add_parser("folders", help=f"list {ROOT_FOLDER} sub folders").set_defaults(func=cmd_folders)

    p = sub.add_parser("list", help=f"list test cases in {ROOT_FOLDER}/<feature>")
    p.add_argument("feature")
    p.add_argument("--not-automated", action="store_true")
    p.set_defaults(func=cmd_list)

    p = sub.add_parser("get", help="full test cases (with steps) as JSON")
    p.add_argument("keys", nargs="*")
    p.add_argument("--feature")
    p.add_argument("--not-automated", action="store_true")
    p.set_defaults(func=cmd_get)

    p = sub.add_parser("create", help=f"create a test case in {ROOT_FOLDER}/<feature> from a JSON spec")
    p.add_argument("feature")
    p.add_argument("spec")
    p.set_defaults(func=cmd_create)

    p = sub.add_parser("update", help="update name/objective/precondition/priority/labels/steps from a JSON spec")
    p.add_argument("key")
    p.add_argument("spec")
    p.set_defaults(func=cmd_update)

    sub.add_parser("verify-sync", help="check every test's TestID exists in the matching MobileTests folder "
                                       "and its @Description equals the Zephyr name").set_defaults(func=cmd_verify_sync)

    p = sub.add_parser("label", help="add a label to a test case")
    p.add_argument("key")
    p.add_argument("label", nargs="?", default=AUTOMATED_LABEL)
    p.set_defaults(func=cmd_label)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
