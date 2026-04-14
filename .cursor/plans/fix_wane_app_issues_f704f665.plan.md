---
name: Fix Wane App Issues
overview: "Debug Mode: Fix 14 UI/animation/UX issues across the Wane Android app using the team-of-agents skill. Water animation performance is the critical priority. Also add a README with sideload/install instructions."
todos:
  - id: triage
    content: "Triage: Analyze all 14 issues against source code (DONE)"
    status: pending
  - id: diagnosis
    content: "Diagnosis: Identify root causes for all issues (DONE)"
    status: pending
  - id: spawn-dev3
    content: "PRIORITY 1 -- Spawn Frontend Dev: Optimize water animation performance and edge quality (CRITICAL - app rejection cause)"
    status: completed
  - id: readme
    content: PRIORITY 1 -- Add README.md with sideload/install instructions for unreleased app
    status: completed
  - id: spawn-dev1
    content: "Spawn Frontend Dev #1: Fix onboarding screen issues (button spacing, logo, counter animation, background)"
    status: completed
  - id: spawn-dev2
    content: "Spawn Frontend Dev #2: Fix session screen UI (icons, spacing, labels, End button, glow, EXIT case)"
    status: completed
  - id: spawn-dev4
    content: "Spawn Frontend Dev #3: Home screen logo, accessibility UX, remove ambient/haptic settings, launcher icons"
    status: completed
  - id: verify
    content: "Lead Verification + Automated Verification: Build, lint, cross-module checks"
    status: in_progress
isProject: false
---

# Fix Wane App Issues (Team of Agents - Debug Mode)

Since this is fixing/debugging an existing project, we follow **Debug Mode** (Section 4 of the skill). No `.team/` setup is needed. We skip HIL gates.

## Execution Order

**Round 1 (CRITICAL -- do first):**
- Water animation performance fix (app rejection cause)
- README.md for sideloading

**Round 2 (after Round 1 verified):**
- All remaining UI/UX fixes in parallel (onboarding, session screen, home screen, settings, launcher icons)

**Round 3:**
- Lead + automated verification of all changes

---

## Debug Phase 1: Triage (Complete)

All 14 issues have been analyzed against the source code. Root causes identified below.

## Debug Phase 2: Diagnosis -- Root Causes

### CRITICAL -- Issue 9: Water animation is slow, laggy, non-crisp edges (APP REJECTION CAUSE)
- **Root cause**: [WaterRenderer.kt](app/src/main/kotlin/com/wane/app/animation/WaterRenderer.kt) uses `FRAME_DT = 0.015f` (hardcoded, not tied to actual frame timing). The shader ([WaterShaders.kt](app/src/main/kotlin/com/wane/app/animation/WaterShaders.kt)) has 8 caustic blobs with per-pixel `exp()` + `sin()` + `length()` calls in a loop, plus 3 wave layers, foam, and ripple. `RENDERMODE_CONTINUOUSLY` in [WaterSurfaceView.kt](app/src/main/kotlin/com/wane/app/animation/WaterSurfaceView.kt) runs every frame. Edge quality issue: `smoothstep(0.012, 0.0, edge1)` is too narrow for crisp foam/edge.
- **Files**: `WaterShaders.kt`, `WaterRenderer.kt`, `WaterSurfaceView.kt`, `WaterThemeCatalog.kt`

### Issue 1: Begin button too close to slider dots
- **Root cause**: In [OnboardingScreen.kt](app/src/main/kotlin/com/wane/app/ui/onboarding/OnboardingScreen.kt), `PageIndicator` at `bottom = 100.dp` and `WaneButton` with `vertical = 40.dp` overlap.

### Issue 2: Onboarding gradient should be a logo
- **Root cause**: [WelcomeStep.kt](app/src/main/kotlin/com/wane/app/ui/onboarding/WelcomeStep.kt) renders `drawGlowOrb()` radial gradient. Replace with `Logo.png`.

### Issue 3: Duration counter animation overshoots on decrement
- **Root cause**: [DurationStep.kt](app/src/main/kotlin/com/wane/app/ui/onboarding/DurationStep.kt) `animateIntAsState` with underdamped spring causes overshoot (25 -> 19 -> 20).

### Issue 4: Onboarding and Home backgrounds differ
- **Root cause**: Onboarding uses 2-color gradient; Home uses 3-color. Onboarding should match Home.

### Issue 5: Remove Ambient Sounds and Haptic Feedback
- **Files**: `SettingsScreen.kt`, `SettingsViewModel.kt`, `PreferencesRepository.kt`, `PreferencesRepositoryImpl.kt`, `PreferenceKeys.kt`, `strings.xml`

### Issue 6: Home screen should show actual logo image
- **Root cause**: `WaneLogo()` uses `Icons.Outlined.WaterDrop` + text. Should use `Logo.png`.

### Issue 7: Accessibility disabled shows toast instead of inline UI
- **Root cause**: `HomeScreen.kt` fires `Toast`. Needs inline button + trust-building message.

### Issue 8: Add launch icons from logo-and-icons
- **Root cause**: Copy mipmap PNGs from `logo-and-icons/res/` to `app/src/main/res/`.

### Issue 10: Glowing component at bottom-left of session screen
- **Root cause**: Likely a caustic blob at a fixed shader position. Needs investigation.

### Issue 11: Bad icons on session screen, need Heroicons with spacing
- **Root cause**: Uses Material Icons. Need better icons with proper spacing.

### Issue 12: End button is unclear and non-functional
- **Root cause**: `EndSessionButton` uses long-press gesture. Users don't discover it.

### Issue 13: Add helper text below session screen icons
- **Root cause**: `BottomToolbar` has icon buttons with no labels.

### Issue 14: Allow case-insensitive EXIT text
- **Root cause**: `ignoreCase = false` in both `SessionViewModel.kt` and `SessionScreen.kt`.

---

## Debug Phase 3: Fix -- Specialist Assignments

### Round 1: CRITICAL (run first, in parallel)

- **Frontend Dev -- Water Animation Performance**
  - Optimize GLSL shaders for performance (reduce per-pixel computation in caustic loop)
  - Improve water edge crispness (adjust `smoothstep` thresholds, possibly add anti-aliasing)
  - Reduce caustic complexity or make count adaptive based on device capability
  - Replace fixed `FRAME_DT` with actual frame delta timing
  - Ensure smooth, professional-looking animation that won't get the app rejected

- **README.md** (orchestrator can handle directly)
  - How to clone and build the project
  - How to install via `adb` sideload
  - How to enable USB debugging
  - How to enable the required Accessibility Service after install

### Round 2: Remaining Fixes (4 agents in parallel, after Round 1 verified)

- **Frontend Dev #1: Onboarding Screen Fixes**
  - Fix button/dot spacing in `OnboardingScreen.kt`
  - Replace gradient orb with Logo.png in `WelcomeStep.kt`
  - Fix counter animation overshoot in `DurationStep.kt`
  - Match onboarding background to Home background
  - Copy `Logo.png` into `app/src/main/res/drawable/`

- **Frontend Dev #2: Session Screen UI Fixes**
  - Replace Material Icons with better alternatives + add spacing
  - Add helper text labels below each icon
  - Redesign End button with clear tap affordance
  - Investigate and remove bottom-left glow
  - Fix case-insensitive EXIT in `SessionViewModel.kt` and `SessionScreen.kt`

- **Frontend Dev #3: Home Screen + Settings + Launch Icons**
  - Replace `WaneLogo()` with actual Logo.png image
  - Replace accessibility Toast with inline UI (button + trust-building message)
  - Remove Ambient Sounds and Haptic Feedback from settings stack
  - Copy launcher icon mipmaps from `logo-and-icons/res/` to `app/src/main/res/`
  - Update adaptive icon XMLs

### Round 3: Verification

- Layer 1 (Self-Verification): Each specialist verifies their own work
- Layer 3 (Lead Verification): Check for cross-file conflicts, interface compliance
- Layer 4 (Automated Verification): Run `./gradlew assembleDebug` and `./gradlew lint`
