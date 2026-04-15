---
name: Dead Code Analysis
overview: Comprehensive dead code analysis of the Wane Android app, identifying 30+ unused symbols across repositories, services, ViewModels, theme tokens, utilities, and build configuration. Following the team-of-agents skill, specialist sub-agents will be spawned to remove dead code by domain.
todos:
  - id: spawn-backend-dev
    content: Spawn Backend Developer to remove dead repository/DAO methods, dead service error variant, unused constructor params, and dead utility functions
    status: completed
  - id: spawn-frontend-dev
    content: Spawn Frontend Developer to remove dead ViewModel state/events, unused theme tokens (typography, motion, colors), and dead WaneTheme object accessors
    status: completed
  - id: spawn-devops
    content: Spawn DevOps to remove unused RECEIVE_BOOT_COMPLETED permission and flag androidTest dependencies
    status: completed
  - id: spawn-test-engineer
    content: Spawn Test Engineer to verify all 5 unit tests still compile and pass after removals, update test fakes if needed
    status: completed
  - id: spawn-security-reviewer
    content: Spawn Security Reviewer for peer verification of Hilt DI graph, Room schema, manifest, and ProGuard integrity after removals
    status: completed
isProject: false
---

# Dead Code Analysis and Removal -- Wane App

## Diagnostic Summary

Four specialist exploration agents analyzed all 70 production Kotlin files and 5 test files. The findings below are organized by domain. Each category maps to a specialist sub-agent that will be spawned for removal.

---

## Category 1: Dead Repository API Surface (Backend Developer)

Unused interface methods (declared + implemented, but never called from any ViewModel, Service, or test):

- **`SessionRepository`** ([app/src/main/kotlin/com/wane/app/data/repository/SessionRepository.kt](app/src/main/kotlin/com/wane/app/data/repository/SessionRepository.kt)):
  - `observeAllSessions()` -- no callers
  - `observeRecentSessions(limit)` -- no callers
- **`ThemeRepository`** ([app/src/main/kotlin/com/wane/app/data/repository/ThemeRepository.kt](app/src/main/kotlin/com/wane/app/data/repository/ThemeRepository.kt)):
  - `observeAllThemes()` -- no callers
  - `observePurchasedThemes()` -- no callers
  - `getThemeById(id)` -- no callers
  - `markThemePurchased(...)` -- no callers
- **`PreferencesRepository`** ([app/src/main/kotlin/com/wane/app/data/repository/PreferencesRepository.kt](app/src/main/kotlin/com/wane/app/data/repository/PreferencesRepository.kt)):
  - `setSelectedThemeId(...)` -- no callers (the observer `observeSelectedThemeId` IS used)
  - `setEmergencyContacts(...)` -- no callers (the observer IS used)

The corresponding DAO methods (`FocusSessionDao.getAllSessions`, `getRecentSessions`, `WaterThemeDao.getAllThemes`, `getPurchasedThemes`, `getThemeById`, `updatePurchaseStatus`) are ONLY called from these dead repository methods, so they cascade as dead too. However, removing DAO methods requires caution since Room test fakes in `StreakCalculatorTest.kt` implement the full DAO interface.

---

## Category 2: Dead Service Layer Code (Backend Developer)

- **`SessionError.AccessibilityServiceDisabled`** in [SessionManager.kt](app/src/main/kotlin/com/wane/app/service/SessionManager.kt) -- defined as a sealed variant but **never emitted** from `SessionManagerImpl`. The UI handler in `HomeScreen` for this error is unreachable.
- **`SessionManagerImpl.preferencesRepository`** in [SessionManagerImpl.kt](app/src/main/kotlin/com/wane/app/service/SessionManagerImpl.kt) -- constructor parameter marked `@Suppress("unused")`, never referenced in the class body.

---

## Category 3: Dead ViewModel / UI State (Frontend Developer)

- **`SettingsViewModel.themeRepository`** in [SettingsViewModel.kt](app/src/main/kotlin/com/wane/app/ui/settings/SettingsViewModel.kt) -- injected via Hilt, never used in class body.
- **`HomeUiState.currentStreak`** -- populated in `HomeViewModel` but never read in `HomeScreen`.
- **`HomeUiState.isSessionActive`** -- populated in `HomeViewModel` but never read in `HomeScreen`.
- **`HomeUiEvent.ChangeDuration`** -- handled in `HomeViewModel.onEvent` but never dispatched from any UI.
- **`SessionUiState.remainingMs`** -- populated but never consumed by `SessionScreen`.
- **`SessionUiState.totalDurationMs`** -- populated but never consumed by `SessionScreen`.
- **`StreakInfo.currentStreak`** and **`longestStreak`** -- computed in `StreakCalculator` but never displayed in any screen (settings only shows `totalSessions`/`totalMinutes`).

---

## Category 4: Dead Theme / Design Tokens (Frontend Developer)

- **Typography** in [Type.kt](app/src/main/kotlin/com/wane/app/ui/theme/Type.kt): `WaneTypography.labelMedium`, `WaneTypography.labelMicro` -- never referenced outside definition.
- **Motion** in [Motion.kt](app/src/main/kotlin/com/wane/app/ui/theme/Motion.kt): `SpringDefault`, `ScreenFadeIn`, `SessionEntry`, `StaggerIntervalHome`, `StaggerIntervalSettings`, `DotTransition` -- never referenced outside definition.
- **Colors** in [Color.kt](app/src/main/kotlin/com/wane/app/ui/theme/Color.kt): `BackgroundAbyss`, `BackgroundOverlay`, `AccentLight`, `MutedTide`, `Divider`, `TextTertiary`, `TextGhost`, `TextGhostActive`, `BorderSubtle` -- only used in `WaneTheme.kt` to populate `WaneColors`, but `WaneTheme.colors` is never accessed.
- **Theme object accessors** in [WaneTheme.kt](app/src/main/kotlin/com/wane/app/ui/theme/WaneTheme.kt): `WaneTheme.colors`, `WaneTheme.typography` -- screens import color vals and `WaneTypography` directly, never through the `WaneTheme` object.

---

## Category 5: Dead / Redundant Utility and Animation Code (Backend Developer)

- **`NotificationUtils.isPhoneNotification`** in [NotificationUtils.kt](app/src/main/kotlin/com/wane/app/util/NotificationUtils.kt) -- never called.
- **`WaterThemeCatalog.getAllVisuals()`** in [WaterThemeCatalog.kt](app/src/main/kotlin/com/wane/app/animation/WaterThemeCatalog.kt) -- never called.
- **`WaterRenderer.clearTouch()`** in [WaterRenderer.kt](app/src/main/kotlin/com/wane/app/animation/WaterRenderer.kt) -- never called.
- **`EmergencySafety.EMERGENCY_NUMBERS`** and **`isEmergencyNumber()`** in [EmergencySafety.kt](app/src/main/kotlin/com/wane/app/util/EmergencySafety.kt) -- redundant. The phone/dialer app is already in the allowlist via `PackageUtils.resolveDialerPackages()`, so notifications from the dialer (including emergency callbacks) are never snoozed. The `isEmergencyNumber` check in `WaneNotificationListener` (line 94) is superseded by the `phoneAndSmsPackages` allowlist check (line 108). Remove `EMERGENCY_NUMBERS`, `isEmergencyNumber()`, and the corresponding check in `WaneNotificationListener.onNotificationPosted`. Keep `NEVER_BLOCK_PACKAGES` and `isNeverBlockPackage()` which ARE actively used for app blocking. Also remove the related test coverage in `EmergencySafetyTest.kt` for the removed members.

---

## Category 6: Dead Build / Manifest Configuration (DevOps)

- **`RECEIVE_BOOT_COMPLETED` permission** in [AndroidManifest.xml](app/src/main/AndroidManifest.xml) -- declared but no matching `<receiver>` with `BOOT_COMPLETED` intent filter exists.
- **Android test dependencies** (`androidTestImplementation`) in [build.gradle.kts](app/build.gradle.kts) -- `espresso-core`, `compose-ui-test-junit4`, `test-ext-junit`, plus `testInstrumentationRunner` are declared, but **no `androidTest/` source set exists**. Either the tests are planned or the dependencies are dead.
- **`WaterTheme` model** ([WaterTheme.kt](app/src/main/kotlin/com/wane/app/shared/WaterTheme.kt)) + `WaterThemeEntity` + `WaterThemeDao` -- the entire theme store/purchase feature has no consumer. Themes are seeded but never listed, displayed, or purchased in any UI. This is a **feature-level dead code** question (may be planned for future).

---

## Execution Plan (Team of Agents -- Debug Mode)

Following the team-of-agents skill delegation mandate, the orchestrator will spawn the following specialists:

### Round 1 (parallel, up to 4 agents)

1. **Backend Developer** -- Remove dead repository methods, dead DAO methods (with care for test fakes), dead service error variant, unused `SessionManagerImpl.preferencesRepository`, dead utility functions (`isPhoneNotification`, `getAllVisuals`, `clearTouch`).
2. **Frontend Developer** -- Remove dead ViewModel state fields, unused event variants, dead theme tokens (typography, motion, colors), dead `WaneTheme` object accessors, unused `SettingsViewModel.themeRepository` dependency.
3. **DevOps** -- Remove unused `RECEIVE_BOOT_COMPLETED` permission from manifest. Flag androidTest dependencies for user decision (keep if tests are planned, remove if not).
4. **Test Engineer** -- After removals, verify all 5 existing unit tests still compile and pass. Update test fakes if DAO methods were removed.

### Round 2 (verification)

5. **Security Reviewer** (peer review) -- Verify removals did not break any Hilt DI graph, Room schema, or manifest-declared component wiring. Ensure ProGuard keep rules are updated if needed.

### Decision Points for User

Before executing, the user should decide on these **feature-level** questions:

- **WaterTheme store/purchase feature**: The entire `WaterTheme`/`WaterThemeEntity`/`WaterThemeDao` + 4 dead `ThemeRepository` methods appear to be scaffolding for a future theme marketplace. Should this be removed as dead code, or kept as planned infrastructure?
- **`StreakInfo.currentStreak`/`longestStreak`**: These are computed but never displayed. Is this planned for future UI, or dead?
- **`HomeUiState.currentStreak`/`isSessionActive`**: Same question -- planned for future home screen enhancements?
- **Android test dependencies**: Are instrumented tests planned? If not, remove `androidTestImplementation` deps and `testInstrumentationRunner`.
