---
name: mobile-challenge-scenarios
description: Suggest challenging mobile-specific test scenarios (app killed or backgrounded mid-flow, network loss, interruptions, rotation, rapid taps, session expiry, UI rendering/distortion after the app is closed or minimised and reopened, etc.) for a Praheal screen or flow, alongside positive and negative checks, and create UI rendering integrity tests. Use when the user asks to suggest tests or scenarios for a screen/feature, when asked to automate a particular test (suggest its challenging variants), and right after a test has been automated (prompt for follow-up challenging tests).
---

# Suggest challenging mobile scenarios

Mobile apps fail at the edges: interrupted flows, flaky networks, lifecycle changes. This skill turns a screen or flow into a short, prioritised list of such scenarios.

## When to use

| Trigger | Output |
|---|---|
| User asks to **suggest tests for a screen/feature** | Positive + Negative + Challenging tables for that screen |
| User asks to **automate a particular test** | 2–4 challenging variants of that test's flow, shown with the plan (do not block the automation) |
| A test has **just been automated and passes** | Prompt: the challenging variants that apply to that flow, and ask which to add to Zephyr / automate |

## How to build the list

1. **Understand the flow.** Read the existing Screen/Steps classes and tests for the screen (`src/main/java/com/praheal/mobile/app/screens/<feature>/`, `src/test/java/com/praheal/mobile/<feature>/`), the Zephyr cases (`python3 .claude/scripts/zephyr.py list <feature>`), and if needed the live screen (`.claude/scripts/ui_dump.sh`). Split the flow into **stages** (e.g. login: fields empty → data typed → Login tapped / request in flight → Choose Patient dialog → patient confirmed → Home).
2. **Apply the checklist below to each stage.** Keep only scenarios that are realistic for that stage and could expose a real defect (lost input, double submission, stuck spinner, crash, wrong screen, data leak). Do not pad.
3. **Skip duplicates** of tests or Zephyr cases that already exist.
4. **Rank** by risk (likelihood × impact). Show at most **5 challenging scenarios per screen** unless the user asks for more.

## Challenge checklist

| Category | Scenarios | How to drive it (Appium / emulator) |
|---|---|---|
| App lifecycle | kill app mid-flow (after typing, right after tapping submit, on a dialog); background and resume; relaunch from recents | `terminateApp` / `activateApp`, `runAppInBackground(Duration)` |
| Network | airplane mode / Wi-Fi + data off before or during a request; network restored and retried; very slow network | `mobile: setConnectivity` (wifi, data, airplaneMode), emulator `mobile: networkSpeed` |
| Interruptions | incoming call or SMS mid-flow; notification tapped mid-flow; system permission dialog | emulator `mobile: gsmCall`, `mobile: sendSms`, `openNotifications` |
| Input & gestures | double / rapid tap on submit; device Back during a request or on a dialog; paste, leading/trailing spaces, max length, emoji, non-numeric in numeric fields; keyboard covering buttons | repeated `click()`, `pressKey(BACK)`, `sendKeys` |
| Device state | rotation (or check the app stays portrait); font scale / display size; dark mode; locale; low battery | `rotate(...)`, `adb shell settings put system font_scale`, `cmd uimode night yes`, `mobile: powerCapacity` |
| Session & state | session/token expired while app open; logout then Back; same user logged in on another device; switch patient mid-flow; reinstall / clear data | API setup, `mobile: clearApp`, `pressKey(BACK)` |
| Data (via API) | empty state, single item, many items / long text, data changed on server while screen open | create/modify data through the API as a precondition, never via UI |
| UI rendering | key elements inside the screen, non-zero size, not overlapping; same positions/sizes after minimise → restore, kill → relaunch, keyboard open → closed, rotate → back; still inside the screen at large font scale | element geometry (`getRect()`), see "UI rendering integrity tests" |
| Security & privacy | password not visible in app switcher / screenshots (`FLAG_SECURE`), sensitive data cleared after logout, error messages don't reveal which credential was wrong | recents screen, `getScreenshotAs` |

## Output format

Be concise: one line per scenario, no prose paragraphs.

```
### <Screen> – suggested tests
Positive
| # | Scenario | Expected |
Negative
| # | Scenario | Expected |
Challenging (ranked)
| # | Stage | Scenario | Expected | Automatable now? |
```

- Never put real credentials or personal values in a suggestion; describe the data (`Patient password`, `Unregistered mobile number`).
- **Expected** is a specific, observable result (exact message, screen, field state). If the expected behaviour is unknown, write `Expected: confirm with product` rather than guessing.
- **Automatable now?** is `Yes` (existing framework + emulator), `Needs API` (data/session setup through an API client the framework doesn't have yet), or `Needs infra` (e.g. real device, second device).
- For the automate / after-automation triggers, show only the Challenging table for the flow just automated.
- For "suggest tests for a screen", add a **UI rendering** table with the applicable rows from "UI rendering integrity tests".

End with one question: which scenarios to add to Zephyr (`/zephyr-add-test`, written in the team format with the name derived from the steps) and/or automate (`/zephyr-automate`). Never create Zephyr cases or write code from this skill without the user choosing.

## UI rendering integrity tests

Use these to verify that a screen's UI elements render correctly and are not distorted after the app is closed or minimised and opened again. Suggest them for every screen (positive rendering check plus lifecycle variants), and create them when the user picks them.

**Why geometry, not images:** the app sets `FLAG_SECURE`, so screenshots are black and pixel comparison is impossible. Rendering is verified from element geometry (`WebElement.getRect()`) and the window size (`androidDriver.manage().window().getSize()`).

**Scenarios per screen** (each one is its own atomic test ending in one `check...`):

| Scenario | Expected |
|---|---|
| Fresh launch | every key element is displayed, has non-zero width/height and lies fully inside the window |
| Fresh launch | no two key interactive elements overlap |
| Minimise → restore (`runAppInBackground`) | key elements have the same position and size as before (±2 px) |
| Kill → relaunch (`terminateApp` / `activateApp`) | key elements have the same position and size as a fresh launch (±2 px) |
| Keyboard opened then closed | layout returns to the pre-keyboard positions (±2 px) |
| Rotate → back to portrait (or app stays portrait) | positions/sizes as before (±2 px) |
| Large font scale (`settings put system font_scale 1.3`) | key elements still inside the window and not overlapping (restore the scale afterwards) |

Measured on the emulator (Pixel, 1344x2992): all 23 login-screen elements had identical bounds after minimise/restore and after kill/relaunch, so a ±2 px tolerance is enough to absorb animations.

**How to build them in this framework** (all rules in `CLAUDE.md` apply):

- **Key elements**: chosen per screen from its Screen class (title, inputs, primary buttons, links) and declared once as a page-wise enum next to the Screen implementing `ScreenElement<XScreen>` (e.g. `LoginFormElement`). Never list elements or labels inline in Steps classes; pass `XElement.values()` to the helpers.
- **Already in the framework** (reuse, see HAT-T21 / `LoginScreenSteps`): `BaseSteps.minimiseAndRestoreApp()`, `captureRects(screen, XElement.values())`, `layoutDifferences(...)`, `ScreenElement`, `LoginFormElement` (tolerance `LAYOUT_TOLERANCE_PX` in `LayoutConstants`), and `LoginScreenSteps.captureLayout()` / `checkLayoutUnchangedSinceCapture()`. Add the within-screen and overlap helpers to `BaseSteps` the first time they are needed.
- **`BaseSteps`** gets the generic, reusable parts (add once, reuse everywhere):
  - action `minimiseAndRestoreApp()` (`runAppInBackground(Duration)`), next to the existing `reopenApp()`;
  - protected helpers that take the screen and its element enum values: capture their rects, find elements outside the window or with zero size, find overlapping pairs, and diff two rect maps with a pixel tolerance (constant in `app/constants`, static imported).
- **`<Feature>ScreenSteps`** gets, using those helpers:
  - action `captureLayout()`: waits for the screen, stores the key elements' rects in a field, returns `this`;
  - `checkKeyElementsRenderedWithinScreen()`: one assertion, the list of out-of-window or zero-size elements is empty (the message names them);
  - `checkKeyElementsDoNotOverlap()`: one assertion, the list of overlapping pairs is empty;
  - `checkLayoutUnchangedSinceCapture()`: waits for the screen, one assertion, the diff against the captured layout is empty (the message lists element, before and after rects).
- **Test shape** (flat chain, ends with a check):
  ```java
  loginScreenSteps
          .captureLayout()
          .minimiseAndRestoreApp();
  loginScreenSteps
          .checkLayoutUnchangedSinceCapture();
  ```
  `minimiseAndRestoreApp()` and `reopenApp()` live in `BaseSteps` and return `void`, so the check starts a new chain on the same steps object.
- **Zephyr**: create the cases with `/zephyr-add-test` in the team format; name derived from the steps, e.g. `Verify patient login screen layout is unchanged after app is minimised and restored`, steps `Note the position of the login form elements` → `Minimise the app and open it again` → `Login form elements are displayed at the same position and size`.

## Known Praheal behaviour (keep updated)

- Killing the app while the Choose Patient dialog is open returns to the login screen (no session persisted before a patient is confirmed).
- Invalid credentials show the inline message `Invalid Mobile No. Or Password.` and stay on Patient Login.
- The app sets `FLAG_SECURE`: screenshots and the app-switcher preview are black.
- Login screen layout is pixel-identical after minimise/restore and after kill/relaunch (23 elements, emulator 1344x2992).
