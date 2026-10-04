# Praheal Mobile Tests

Appium + TestNG + Allure framework for the Praheal Android app (`com.praheal.main`, React Native).

Setup: copy `secrets.properties.example` to `secrets.properties` and fill in the test patient; put the APK at the `appPath` from `config.properties` (APKs are git-ignored).

Run: start Appium, then `mvn test` (or `mvn test -Dtest=LoginScreenTests`); report: `allure serve allure-results`.

## Framework rules

1. **Screen / Steps split.** Every screen has two classes in `app/screens/<feature>/`:
   - `XScreen extends BaseScreen`: locators only (`@AndroidFindBy` fields, package-private). No actions, no assertions.
   - `XScreenSteps extends BaseSteps`: actions and `check...` methods. Holds its `XScreen`; declares no locators.
   - Enforced: `StepInjector` throws if a Steps class declares a `WebElement`/`List`/`@...FindBy` field.
2. Example: `LoginScreen` (locators) + `LoginScreenSteps` (actions + checks).
3. **One verification per check method.** Each `check...` method makes exactly one assertion. Split multi-assert checks.
4. **Screenshots are automatic for `check...` methods.** `StepInjector` wraps every Steps class and attaches a screenshot after each `check...` call (pass or fail). Do not call `attachScreenshot()` inside check methods.
5. **Tests are atomic and end with a `check...` step.** One behaviour per test, independent of other tests (app is terminated and relaunched around every test). Enforced: `PrahealListener` fails any test whose last top-level step is not a `check...` method.
6. **No assertions in tests.** Assertions live only in `check...` methods of Steps classes. Enforced: `FrameworkRulesTests.assertionsAreOnlyInStepsClasses` (no class outside `*Steps` may use TestNG/JUnit/AssertJ/Hamcrest assertions).
7. **No logic at test level.** A test is a flat chain of Steps calls: no `if`/loops, no driver, no `WebElement`/`By`, no Screen classes. Enforced (except control flow): `FrameworkRulesTests.testsDoNotUseDriverOrElements` and `testsDoNotUseScreenClasses`.
8. **Constants are `public static final` and always static imported.** Constant classes are `final` with a private constructor; use `import static ...CONSTANT;`, never `ClassName.CONSTANT`. Enforced (except the import style): `FrameworkRulesTests.constantsArePublicStaticFinal` for `app/constants/**` and `UsersPool`.
9. **Test users live in `UsersPool`** (`app/users/UsersPool.java`) and reach tests through a data provider as `PrahealPatient`; never hard-code credentials in tests or steps. `UsersPool` reads real values at runtime via `Secrets` from env vars (`PRAHEAL_PATIENT_MOBILE_NUMBER`, `PRAHEAL_PATIENT_PASSWORD`) or the git-ignored `secrets.properties` (copy `secrets.properties.example`); real credentials are never committed. Invalid/throwaway data (passwords, unregistered mobile numbers) is generated fresh on every run with `RandomTestData` inside the data provider, never hard-coded.
10. **No hard-coded data in Steps classes.** Values (expected texts, attribute names, labels, element lists, timeouts) come from page-wise constants or enums, never from inline literals (only `@Step` texts and assertion messages are written inline):
   - page-specific constants: `app/constants/<feature>/<Feature>Constants` (e.g. `app/constants/login/LoginConstants`); shared ones: `app/constants/` (`AppIcons`, `ElementAttributes`, `LayoutConstants`); always `public static final` and static imported;
   - groups of a screen's elements: an enum next to the Screen class implementing `ScreenElement<XScreen>` (e.g. `LoginFormElement`): one constant per element mapped to the Screen's locator, label derived from the constant name; Steps iterate `XElement.values()` instead of building maps or lists by hand.

Architecture rules run without a device: `mvn test -Dtest=FrameworkRulesTests`.

## Conventions

- Action methods return `this` (fluent); `check...` methods return `void`.
- Every public Steps method has an Allure `@Step`.
- Test classes extend `BaseMobileTest`; Steps fields are injected automatically (field type must end in `Steps`).
- Every test has `@PrahealLabels.TestID("HAT-T<n>")` with its Zephyr test case key (enforced: `FrameworkRulesTests.testsHaveZephyrTestId`) and `@Description` equal to the Zephyr case name. Omit `@PrahealLabels.ScenarioID` for now.
- Use existing tests and existing Zephyr cases as the reference for new ones (structure, naming, wording).
- Locators: prefer `accessibility` (content-desc) and `@hint`; avoid `@text` on input fields (it changes as the user types). Icon-only buttons use glyphs from `AppIcons`, written as `\uXXXX` escapes.
- Do not add code comments.
- The app sets `FLAG_SECURE`, so screenshots/Inspector preview are black; use the page source (`.claude/scripts/ui_dump.sh`) to find locators.

## Zephyr Scale

- Test cases live in Jira project `HAT`, folder **`MobileTests`**. Its sub folders mirror this repo's feature packages: `MobileTests/login` ↔ `app/screens/login` + `src/test/.../login`, `MobileTests/home` ↔ `app/screens/home` + `src/test/.../home`. A new feature gets the same name in both places.
- Read from and add to `MobileTests/<feature>` only; never touch other folders.
- Helper: `python3 .claude/scripts/zephyr.py folders | list <feature> [--not-automated] | get <KEY...> | get --feature <feature> | create <feature> <spec.json> | update <KEY> <spec.json> | label <KEY>`.
- **Sync check:** `python3 .claude/scripts/zephyr.py verify-sync` fails if a test's `HAT-T` key does not exist, is not in `MobileTests/<its feature package>`, its `@Description` differs from the Zephyr name, or a case labelled `automated` has no test. Run it after adding, renaming or automating cases (needs the Zephyr token, so it is not part of `mvn test`).
- Current mapping: `MobileTests/login` HAT-T11..T16, HAT-T19..T21 → `LoginScreenTests`; `MobileTests/home` HAT-T17..T18 → `HomeScreenTests`.
- Skills: `/zephyr-automate` (read cases from Zephyr and automate them), `/zephyr-add-test` (create cases in Zephyr in the team's format), `/mobile-challenge-scenarios` (suggest positive, negative and challenging mobile scenarios: app killed/backgrounded mid-flow, network loss, interruptions, rapid taps, session expiry). Suggestions are offered when a test is requested for automation and again after it is automated; nothing is created without the user choosing.
- After a case is automated and its test passes, add the `automated` label in Zephyr.
- A case's name and objective (and the test's `@Description`/method name) describe exactly the actions in its steps and the verification in its final expected result.
- Only clearly written cases are automated: clear precondition (user + starting screen), UI-only steps, specific expected results. Data that a test verifies on screen (tables, lists, counts) is created via API as a precondition, never through UI steps. Unclear cases are not automated; the user gets the problems plus a suggested rewrite and is asked whether to apply it in Zephyr using the skills.
- **No sensitive data in Zephyr.** Never write a real mobile number, password, OTP, email, token, patient name or any other personal/secret value into a Zephyr case (name, objective, precondition, steps, test data, expected result). Describe the data instead (`Patient mobile number`, `Patient password`, `Randomly generated password`); real values live only in `UsersPool` (or are generated at runtime with `RandomTestData`). `zephyr.py create`/`update` refuse specs containing `UsersPool` values, mobile numbers, emails, `password: ...` style values or tokens. Applies when adding cases and when automating them.
- The API token is `ZEPHYR_API_TOKEN`, kept only in `.claude/settings.local.json` (git-ignored). Never put it in code, docs or commits.
