---
name: zephyr-automate
description: Read test cases from Zephyr Scale (project HAT, folder MobileTests/<feature>) and automate them in this Appium framework. Use when asked to automate Zephyr tests, automate a HAT-T key, or pick up manual test cases from MobileTests/login, MobileTests/home or another MobileTests sub folder.
---

# Automate Zephyr test cases

Zephyr project `HAT`, root folder `MobileTests`. Each sub folder maps 1:1 to a feature package in this repo:

| Zephyr folder | Screen/steps package | Test package |
|---|---|---|
| `MobileTests/<feature>` | `src/main/java/com/praheal/mobile/app/screens/<feature>/` | `src/test/java/com/praheal/mobile/<feature>/` |

All Zephyr calls go through `.claude/scripts/zephyr.py` (token comes from `ZEPHYR_API_TOKEN`, set in `.claude/settings.local.json`). Never print or commit the token.

## Workflow

1. **Pick the cases.**
   - Specific keys: `python3 .claude/scripts/zephyr.py get HAT-T12 HAT-T13`
   - A whole feature: `python3 .claude/scripts/zephyr.py list <feature> --not-automated`, then `get --feature <feature> --not-automated`
   - Unknown feature: `python3 .claude/scripts/zephyr.py folders`
   Skip cases whose key already appears in a `@PrahealLabels.TestID("...")` in `src/test` (`grep -rn 'TestID("HAT-T12")' src/test`).

2. **Review each case before automating it. Do not automate a case that is not clearly written.** A Zephyr test must have a clear precondition and clear UI steps. A case passes review only if all of these hold:
   - **Precondition** names the user/role (`Patient user is available...`) and the starting screen or state (`Patient login screen is open`, `Patient is logged in`).
   - **Test data prerequisites are set up through APIs.** If the test checks data shown on screen (tables, lists, cards, counts such as appointments, plans, kritis, notifications), the precondition must state the exact data to create, e.g. `1 upcoming appointment for the patient on <date> created via API`, and that it is created via API, not by clicking through the UI. A case that expects data to "already exist" or sets it up with UI steps fails review.
   - **Steps are UI steps only**: one concrete user action per step on a named element (`Click on the Login button`), no setup work hidden in the steps, no vague actions (`go to the page`, `check everything`).
   - **Expected results are observable and specific**: the exact element, text or value to see (`Error message "Invalid Mobile No. Or Password." is displayed`), not `works correctly` / `as expected`.
   - **No sensitive data in the case**: `zephyr.py get` returns a `sensitiveData` list per case; it must be empty, and the case must not contain any real credential or personal value. If it does, the case fails review: suggest the described replacement (`Patient password`, ...) and, with the user's agreement, fix it with `/zephyr-add-test` (`update`). Never copy such values from Zephyr into code; credentials come only from `UsersPool` via a data provider, invalid/throwaway data from `RandomTestData`.
   - **Test data is identifiable**: described clearly (`Patient password`, `Invalid password`) and available in `UsersPool` or creatable via API.
   - No contradictions between objective, precondition, steps and expected results.
   - **Name and objective match the steps**: they describe exactly the actions in the steps and the verification in the final expected result. A name that promises a different outcome than the last expected result (e.g. name `Verify patient logs in with valid credentials` while the steps end with `Choose Patient dialog is displayed`), or a condition the steps don't cover, fails review; suggest a name/objective derived from the steps.

   For every case that fails review:
   - skip it: write no code, add no label;
   - tell the user `HAT-T<n> needs correction before it can be automated`, list each problem, and give a concrete suggested rewrite (precondition with the API-created data, numbered UI steps, expected results);
   - ask the user whether these corrections should be applied in Zephyr using the skills (`/zephyr-add-test` updates the case with `python3 .claude/scripts/zephyr.py update HAT-T<n> spec.json`). Apply them only after the user agrees, then re-review the corrected case before automating it.

   If a case needs API-created data and the framework has no API client for that data yet, do not fall back to UI setup; tell the user which API setup is missing and ask how to proceed.

3. **Plan each reviewed case.** From objective, precondition and steps decide:
   - which screen(s) are involved, which preconditions the app state must satisfy (logged in or not, which tab), and which API calls create the test data,
   - which actions and which single verification point the test ends with. A Zephyr case with several expected results becomes several atomic tests (one `check...` each), all tagged with the same Zephyr key.
   - Alongside the plan, list 2–4 challenging variants of this flow using the `mobile-challenge-scenarios` skill (app killed mid-flow, network loss, interruptions, rapid taps, ...). This is informational; do not block or expand the automation unless the user asks.

4. **Get real locators.** Put the emulator on the screen in question and run `.claude/scripts/ui_dump.sh`. Use `accessibility` (content-desc) or `@hint`; avoid `@text` on inputs. Icon-only buttons: add the glyph to `AppIcons` as a `\uXXXX` escape. The preview/screenshots are black (`FLAG_SECURE`), so rely on the dump only.

5. **Write the code, following every rule in `CLAUDE.md` and using the existing tests as reference.** Read `src/test/java/com/praheal/mobile/login/LoginScreenTests.java` and `app/screens/login/` (Zephyr HAT-T11 to HAT-T16) first and mirror their structure, naming and annotations:
   - locators go in `<Feature>Screen` (package-private `@AndroidFindBy` fields), actions and `check...` methods in `<Feature>ScreenSteps`; reuse existing methods before adding new ones;
   - each `check...` method makes exactly one assertion and has an `@Step`; no `attachScreenshot()` calls (automatic);
   - tests extend `BaseMobileTest`, are a flat chain of steps calls ending in a `check...` call, with no assertions, driver, elements, Screen classes, `if` or loops;
   - the test implements exactly the Zephyr steps and ends with the `check...` for the final expected result, so `@Description` (the Zephyr name), the method name and the steps chain all describe the same actions and verification;
   - no hard-coded data in Steps classes: expected texts, attribute names, labels and element groups go into page-wise constants (`app/constants/<feature>/<Feature>Constants`) or an element enum next to the Screen implementing `ScreenElement` (see `LoginFormElement`); reuse existing ones before creating new ones;
   - constants are `public static final` in `app/constants` and static imported; credentials only from `UsersPool` via a data provider;
   - no code comments.

   Test method template:
   ```java
   @Test
   @Epic("<Feature> Screen")
   @Story("<Zephyr name or group>")
   @Description("<Zephyr case name, verbatim>")
   @PrahealLabels.TestID("HAT-T12")
   @Severity(SeverityLevel.NORMAL)
   public void verifySomething() {
       someScreenSteps
               .doAction()
               .checkSomething();
   }
   ```
   Map Zephyr priority to `@Severity`: High → `CRITICAL`, Normal → `NORMAL`, Low → `MINOR`. Do not add `@PrahealLabels.ScenarioID` for now.

6. **Verify.**
   - `mvn test -Dtest=FrameworkRulesTests` must pass.
   - `python3 .claude/scripts/zephyr.py verify-sync` must pass: every test's `HAT-T` key exists in the matching `MobileTests/<feature>` folder and its `@Description` equals the Zephyr case name.
   - Run the new tests on the emulator: `mvn test -Dtest=<Feature>ScreenTests#<method>` (Appium must be running on 127.0.0.1:4723 and no Appium Inspector session open). A test is done only when it passes; report failures with the assertion message, do not weaken the check to make it pass.

7. **Mark as automated in Zephyr** only after the tests pass: `python3 .claude/scripts/zephyr.py label HAT-T12` (adds the `automated` label), then run `verify-sync` once more. Tell the user which keys were labelled.

## Report back

List each Zephyr key with the test method(s) created, run result, and whether it was labelled. Then list every case that failed review under **Needs correction**, with its problems and suggested rewrite, and ask whether to apply the corrections in Zephyr using the skills.

Finally, for each flow that was automated and passes, use the `mobile-challenge-scenarios` skill to prompt the challenging follow-up tests that apply (Challenging table only, ranked, max 5), and ask which to add to Zephyr and/or automate.
