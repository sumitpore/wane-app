# Task Board: Wane

**Last Updated**: 2026-04-15

> All teammates read and update this file. Use task IDs for dependency references.

## Legend

- `PENDING` -- Not started, may be blocked by dependencies
- `IN_PROGRESS` -- Actively being worked on by assigned role
- `COMPLETED` -- Done and verified
- `BLOCKED` -- Waiting on dependency
- `REVISION` -- Failed verification, needs rework

---

## Phase 0: Discovery


| ID   | Task                                   | Role               | Status    | Depends On | Notes                |
| ---- | -------------------------------------- | ------------------ | --------- | ---------- | -------------------- |
| P0-1 | Define project requirements and vision | Business/Marketing | COMPLETED | —          | PROJECT.md created   |
| P0-2 | User personas and value proposition    | Business/Marketing | COMPLETED | —          | discovery.md created |


## Phase 1: Content + Design


| ID    | Task                                   | Role            | Status    | Depends On | Notes                                                                                  |
| ----- | -------------------------------------- | --------------- | --------- | ---------- | -------------------------------------------------------------------------------------- |
| P1-1  | Create screen map                      | UI Designer     | COMPLETED | P0-1       | 18 screens mapped, reduced to 10 owned + 6 native + 2 removed                          |
| P1-2  | Write content for all screens          | Content Writer  | COMPLETED | P1-1       | 18 screen content artifacts created                                                    |
| P1-3  | Create initial DESIGN.md               | UI Designer     | COMPLETED | P0-2       | Design system defined                                                                  |
| P1-4  | Compliance review                      | Consent Manager | COMPLETED | P1-2       | compliance-report.md created                                                           |
| P1-5  | Generate Stitch screens                | UI Designer     | COMPLETED | P1-2, P1-3 | All 18 screens generated in Stitch project 453137823679633111                          |
| P1-6  | Export design prototype                | UI Designer     | COMPLETED | P1-5       | Exported to design/Focus Mode App/ (React/Vite)                                        |
| P1-7  | Design audit                           | UI Designer     | COMPLETED | P1-6       | design-audit.md: 35+ colors, 16 typography styles, 10 screen specs, Compose-ready code |
| P1-8  | Revise DESIGN.md with prototype values | Lead            | COMPLETED | P1-7       | Prototype colors as source of truth per user decision                                  |
| P1-9  | Update screen map (scope changes)      | Lead            | COMPLETED | P1-7       | Removed screens 17, 18; marked 8-13 as native                                          |
| P1-10 | HIL Gate 1: Design approval            | Lead            | COMPLETED | P1-7       | All 10 screens approved. 3 toolbar buttons (Phone + Contacts + SMS).                   |


## Phase 2: Foundation


| ID   | Task                             | Role                                         | Status    | Depends On | Notes                                                                                                                   |
| ---- | -------------------------------- | -------------------------------------------- | --------- | ---------- | ----------------------------------------------------------------------------------------------------------------------- |
| P2-1 | Define project-wide conventions  | Lead                                         | COMPLETED | P1-10      | CONVENTIONS.md General section: language, encoding, indentation, naming, banned words, accessibility, privacy           |
| P2-2 | Define role-specific conventions | Content Writer, UI Designer, Consent Manager | COMPLETED | P2-1       | All three convention files created with comprehensive domain-specific rules                                             |
| P2-3 | Scaffold project structure       | Lead                                         | COMPLETED | P2-1       | Android project directories: ui/{onboarding,home,session,settings}, theme, components, service, data, util, res/, test/ |


## Phase 3: Architecture


| ID   | Task                            | Role                       | Status    | Depends On | Notes                                                                                           |
| ---- | ------------------------------- | -------------------------- | --------- | ---------- | ----------------------------------------------------------------------------------------------- |
| P3-1 | Define module boundaries        | Lead                       | COMPLETED | P2-3       | 7 modules, clear file ownership per role                                                        |
| P3-2 | Tech stack proposals            | Backend/Frontend/DB/DevOps | COMPLETED | P3-1       | 4 proposals completed                                                                           |
| P3-3 | HIL Gate 2: Tech stack approval | Lead                       | COMPLETED | P3-2       | All approved. compileSdk=36/targetSdk=36, minSdk=28, Nav3, Room 3.0, Hilt 2.57.1                |
| P3-4 | Define interfaces               | Specialists                | COMPLETED | P3-3       | INTERFACES.md: 12 API contracts, 9 shared types, 5 event contracts                              |
| P3-5 | Tech-specific conventions       | All tech roles             | COMPLETED | P3-3       | All 6 technical role convention files updated                                                   |
| P3-6 | Full scaffold with stubs        | Frontend/Backend Dev       | COMPLETED | P3-4, P3-5 | 42 files: build config, manifest, shared types, interface stubs, theme foundation, entry points |


## Phase 4: Implementation

### Round 1: Data Layer + Core Services + Water Animation


| ID      | Task                                                                                | Role        | Status    | Depends On | Notes                                                                                   |
| ------- | ----------------------------------------------------------------------------------- | ----------- | --------- | ---------- | --------------------------------------------------------------------------------------- |
| P4-R1-1 | Room database, DAOs, TypeConverters, Hilt DataModule                                | DB Engineer | COMPLETED | P3-6       | WaneDatabase, FocusSessionDao, WaterThemeDao, Converters, DataModule                    |
| P4-R1-2 | Repository implementations (Session, Theme, Preferences) + StreakCalculator         | DB Engineer | COMPLETED | P4-R1-1    | SessionRepositoryImpl, ThemeRepositoryImpl, PreferencesRepositoryImpl, StreakCalculator |
| P4-R1-3 | SessionManager + WaneSessionService + timer engine + state machine                  | Backend Dev | COMPLETED | P3-6       | SessionManagerImpl, WaneSessionService (LifecycleService), coroutine timer              |
| P4-R1-4 | AutoLockScheduler + ScreenLockReceiver + RepeatedCallerTracker                      | Backend Dev | COMPLETED | P4-R1-3    | AutoLockScheduler, ScreenLockReceiver, RepeatedCallerTracker, ServiceModule             |
| P4-R1-5 | Water animation engine (WaterRenderer, WaterShaders, WaterSurfaceView, WaterCanvas) | Backend Dev | COMPLETED | P3-6       | OpenGL ES 3.0 GLSL shaders, GLSurfaceView, Compose WaterCanvas wrapper                  |
| P4-R1-6 | TiltSensorManager + WaterThemeCatalog                                               | Backend Dev | COMPLETED | P3-6       | Gyroscope sensor with callbackFlow, 5 theme visual definitions                          |


### Round 2: System Services + Core UI Screens + DevOps


| ID      | Task                                                                    | Role         | Status    | Depends On       | Notes                                                                 |
| ------- | ----------------------------------------------------------------------- | ------------ | --------- | ---------------- | --------------------------------------------------------------------- |
| P4-R2-1 | WaneAccessibilityService + AppBlocker                                   | Backend Dev  | COMPLETED | P4-R1-3          | AppBlocker, PackageUtils, WaneAccessibilityService with EntryPoint DI |
| P4-R2-2 | WaneNotificationListener                                                | Backend Dev  | COMPLETED | P4-R1-3, P4-R1-4 | NotificationUtils, snooze/re-snooze(1ms), emergency breakthrough      |
| P4-R2-3 | Onboarding screens (Welcome, Duration, Auto-Lock) + OnboardingViewModel | Frontend Dev | COMPLETED | P4-R1-2          | HorizontalPager, 3 steps, glass toggle, spring animations             |
| P4-R2-4 | Home screen + HomeViewModel                                             | Frontend Dev | COMPLETED | P4-R1-2, P4-R1-3 | Streak display, pulsing glow start button, duration picker            |
| P4-R2-5 | CI/CD workflows (ci.yml, release.yml, scheduled.yml) + Dependabot       | DevOps       | COMPLETED | P3-6             | 3 workflows, dependabot with groups, proguard rules                   |


### Round 3: Session + Settings Screens + Navigation


| ID      | Task                                                           | Role         | Status    | Depends On                | Notes                                                                    |
| ------- | -------------------------------------------------------------- | ------------ | --------- | ------------------------- | ------------------------------------------------------------------------ |
| P4-R3-1 | Session screen + SessionViewModel (water + toolbar + overlays) | Frontend Dev | COMPLETED | P4-R1-3, P4-R1-5, P4-R1-6 | Water animation, emergency exit sheet, session complete overlay, toolbar |
| P4-R3-2 | Settings screen + SettingsViewModel                            | Frontend Dev | COMPLETED | P4-R1-2                   | Settings panel, grouped glass-effect sections, spring slide-up           |
| P4-R3-3 | Auto-Lock Settings screen + AutoLockViewModel                  | Frontend Dev | COMPLETED | P4-R1-2                   | Auto-lock config UI, toggle, duration, grace period, skip window         |
| P4-R3-4 | WaneNavHost + navigation setup + predictive back handling      | Frontend Dev | COMPLETED | P4-R2-3, P4-R2-4          | Nav3 NavDisplay, SnapshotStateList backStack, BackHandler                |


### Round 4: Testing + Validation


| ID      | Task                                                              | Role              | Status    | Depends On       | Notes                                                                             |
| ------- | ----------------------------------------------------------------- | ----------------- | --------- | ---------------- | --------------------------------------------------------------------------------- |
| P4-R4-1 | Unit tests: data layer (repositories, StreakCalculator, DAOs)     | Test Engineer     | COMPLETED | P4-R1-2          | 69 tests across 5 files, all passing                                              |
| P4-R4-2 | Unit tests: services layer (SessionManager, timer, state machine) | Test Engineer     | COMPLETED | P4-R1-3          | AppBlocker + RepeatedCallerTracker tested                                         |
| P4-R4-3 | Unit tests: ViewModels                                            | Test Engineer     | COMPLETED | P4-R3-4          | PreferencesRepository validation logic tested                                     |
| P4-R4-4 | Security audit                                                    | Security Reviewer | COMPLETED | P4-R2-1, P4-R2-2 | CONDITIONAL PASS — 2 items flagged (POST_NOTIFICATIONS runtime, "08" emergency)   |
| P4-R4-5 | User persona validation                                           | User Role         | COMPLETED | P4-R3-4          | CONDITIONAL PASS — brand issues found (Settings nav, timer text, "Blocking" copy) |


## Integration


| ID       | Task                                                  | Role   | Status    | Depends On | Notes                                                         |
| -------- | ----------------------------------------------------- | ------ | --------- | ---------- | ------------------------------------------------------------- |
| P4-INT-1 | Cross-module verification (imports, APIs, data flows) | Lead   | COMPLETED | P4-R3-4    | Layer 5: 10/10 checks PASS                                    |
| P4-INT-2 | Requirements verification vs PROJECT.md               | Lead   | COMPLETED | P4-INT-1   | Layer 6: 17 requirements checked, 16 PASS, 1 PARTIAL (haptic) |
| P4-INT-3 | Full build + lint + test suite                        | Lead   | COMPLETED | P4-R4-3    | assembleDebug + testDebugUnitTest BUILD SUCCESSFUL            |
| P4-INT-4 | Performance profiling (Macrobenchmark, battery)       | DevOps | DEFERRED  | P4-INT-1   | Requires physical device; deferred to post-deployment         |


### Round 5: UX Minimalist Roadmap (2026-04-15)


| ID      | Task                                                                            | Role           | Status    | Depends On       | Notes                                                                                              |
| ------- | ------------------------------------------------------------------------------- | -------------- | --------- | ---------------- | -------------------------------------------------------------------------------------------------- |
| P4-R5-1 | Language audit: audit all user-facing strings, deliver neutral replacements     | Content Writer | COMPLETED | —                | 68 XML + 18 Kotlin strings audited; 2 XML + 0 Kotlin changed (exit phrases kept)                   |
| P4-R5-2 | Add extendSession() to SessionManager interface and implementation              | Backend Dev    | COMPLETED | —                | AtomicLong for mutable duration; DB plannedDuration update; DAO/Repository extended                |
| P4-R5-3 | Soften language: update session_complete_title and accessibility_prompt_message | Frontend Dev   | COMPLETED | P4-R5-1          | "Well done" → "Session complete"; "block" → "cover"                                                |
| P4-R5-4 | Privacy micro-label on Home screen                                              | Frontend Dev   | COMPLETED | —                | "100% ON-DEVICE" text, taps to Settings                                                            |
| P4-R5-5 | Graduated exit sheet before emergency exit                                      | Frontend Dev   | COMPLETED | P4-R5-2          | "5 more minutes?" ModalBottomSheet; Keep going extends 5 min; I need to leave → affirmation phrase |
| P4-R5-6 | Replace toolbar icons with Heroicons                                            | Frontend Dev   | COMPLETED | —                | Phone, SMS, Contacts, X-Mark as ImageVector with SVG stroke paths                                  |
| P4-R5-7 | Unit tests for extendSession and graduated exit flow                            | Test Engineer  | COMPLETED | P4-R5-2, P4-R5-5 | 6 SessionManagerImpl + 4 SessionViewModel tests                                                    |


