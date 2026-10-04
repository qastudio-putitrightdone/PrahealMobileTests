---
name: zephyr-add-test
description: Add (create) test cases in Zephyr Scale under project HAT, folder MobileTests/<feature>, written in the team's format. Use when asked to add, create, write up or sync test cases to Zephyr, or when an automated test in this repo has no Zephyr key yet.
---

# Add test cases to Zephyr

Zephyr project `HAT`, root folder `MobileTests`; the sub folder is the feature name and matches the repo package (`login`, `home`, ...). A new feature sub folder is created automatically by the script.

All Zephyr calls go through `.claude/scripts/zephyr.py` (token from `ZEPHYR_API_TOKEN` in `.claude/settings.local.json`). Never print or commit the token.

## Team format

Always use existing cases as the reference before drafting. Read a few first, e.g. `python3 .claude/scripts/zephyr.py get HAT-T11 HAT-T16 HAT-T3 HAT-T7` and the cases already in `MobileTests/<feature>`, and match their wording.

- **name**: `Verify <screen> displays <element>` for presence checks (`Verify patient login screen displays Mobile No. field`); `Verify <behaviour>` otherwise. One behaviour per case.
- **objective**: one `To verify ...` sentence.
- **Name and objective must match the steps.** They describe exactly the actions in the steps and the verification in the final expected result, nothing more and nothing else. Write the steps first, then derive the name/objective from them:
  - the verification named in the name/objective is the one in the last step's `expectedResult` (e.g. steps end with `Choose Patient dialog is displayed` → `Verify Choose Patient dialog is displayed after patient logs in with valid credentials`, not `Verify patient logs in with valid credentials`);
  - every condition in the name (valid/invalid data, which field, which button) appears in the steps or test data, and the steps do not test anything the name does not mention;
  - before creating or updating, re-read each case and fix any mismatch.
- **precondition**: numbered lines stating the user and the starting screen, e.g. `1. Patient user is available in Praheal application\n2. Patient login screen is open` or `...\n2. Patient is logged in`. If the case checks data shown on screen (tables, lists, cards, counts), the precondition states the exact data and that it is created via API, e.g. `3. 1 upcoming appointment for the patient is created via API`. Never put data setup in the UI steps.
- **steps**: one user action per step, each with its own short `expectedResult` stated as an outcome, not as "Verify that" (`Mobile number is entered`, `Password is entered (masked)`, `Login button is displayed`). Presence checks are a single step: `Observe the login form on the patient login screen` → `<Element> is displayed`.
- **testData**: describe the data, never the real value (`Patient mobile number`, `Patient password`); real values live in `UsersPool`.
- **No sensitive data in Zephyr.** Never write a real mobile number, password, OTP, email, token, patient name or any other personal/secret value into a Zephyr case (name, objective, precondition, steps, test data, expected result). Describe the data instead (`Patient mobile number`, `Patient password`, `Randomly generated password`); real values live only in `UsersPool` (or are generated at runtime with `RandomTestData`). `zephyr.py create`/`update` refuse specs containing `UsersPool` values, mobile numbers, emails, `password: ...` style values or tokens.
- **priority**: `High` for core paths (critical fields, successful login), `Normal` otherwise, `Low` for cosmetic. **status**: `Draft` unless told otherwise.
- **labels**: `smoke` for basic screen-presence and happy-path checks; `automated` only when a passing automated test with this key exists in the repo. Do not invent requirement labels (`AUTH-01`, ...); add one only if the user gives it.

## Workflow

1. **Collect the cases.** From the user's description, or from existing tests in `src/test/java/com/praheal/mobile/<feature>/` that have no Zephyr key (their `@PrahealLabels.TestID` is not `HAT-T<number>`). For an automated test, derive the steps from its steps chain and `@Step` texts, the expected result from its final `check...` step, and then the name/objective from those steps (see "Name and objective must match the steps").

2. **Check for duplicates.** `python3 .claude/scripts/zephyr.py list <feature>`; do not create a case whose name/behaviour already exists, reuse its key instead.

3. **Check for sensitive data**, then **confirm before writing.** Re-read every field of each draft and replace any real credential or personal value with a description. If `zephyr.py` refuses a spec, fix the listed fields; never work around the check.

    Show the user the drafted cases (name, priority, steps, expected result) and the target folder, and create them only after they agree. Creating cases is visible to the whole team.

4. **Create** each case from a JSON spec saved in the scratchpad:
   ```json
   {
     "name": "Verify password is displayed when show password icon is clicked on patient login screen",
     "objective": "To verify the typed password becomes visible after clicking the show password icon",
     "precondition": "1. Patient user is available in Praheal application\n2. Patient login screen is open",
     "priority": "Normal",
     "labels": ["automated"],
     "steps": [
       {"description": "Enter the patient password in the Password field", "testData": "Patient password", "expectedResult": "Password is entered (masked)"},
       {"description": "Click on the show password icon in the Password field", "expectedResult": "Password is displayed in plain text"}
     ]
   }
   ```
   `python3 .claude/scripts/zephyr.py create <feature> spec.json` prints the new key (e.g. `HAT-T25`).

   To fix an existing case instead, use the same spec shape (any subset of fields) with `python3 .claude/scripts/zephyr.py update HAT-T25 spec.json`; `steps` in an update replace all existing steps.

5. **Link back to code.** Put the key in the test's `@PrahealLabels.TestID("HAT-T25")` (no `ScenarioID` for now), set its `@Description` to the Zephyr case name (which must describe the test's own steps chain and final `check...`), keep the method name consistent with it (`verify<Verification>After<Action>` style), and run `mvn test -Dtest=FrameworkRulesTests` (it fails if any test lacks a `HAT-T<n>` key) and `python3 .claude/scripts/zephyr.py verify-sync` (it fails if a key is missing in Zephyr, sits in the wrong `MobileTests` folder, its name differs from `@Description`, or an `automated` case has no test). After updating a case name with `update`, update the test's `@Description` too and re-run `verify-sync`.

## Report back

List each created key with its name and folder, and which test methods now carry it.
