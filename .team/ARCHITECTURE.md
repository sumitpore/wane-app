# Architecture: Wane

**Created**: 2026-04-10
**Last Updated**: 2026-04-13
**Status**: HIL Gate 2 Approved

## 1. System Overview

Wane is a fully local Android application with no server component. The architecture is a single-app, single-process Android application built with Kotlin and Jetpack Compose. The app has four primary layers:

1. **UI Layer**: Jetpack Compose screens, components, theme, and navigation
2. **Services Layer**: Android system services (AccessibilityService, NotificationListenerService), foreground service for session management, and the water animation rendering engine
3. **Data Layer**: Local-only persistence (session history, user preferences, settings)
4. **Build/Infrastructure**: Gradle build system, CI/CD, signing, Play Store deployment

No network calls are made (except future opt-in analytics). All data stays on device.

## 2. Module Map

| Module | Path | Owner Role | Description |
| ------ | ---- | ---------- | ----------- |
| UI / Screens | `app/src/main/kotlin/com/wane/app/ui/` | Frontend Developer | Compose screens (onboarding, home, session, settings), shared components, theme, navigation |
| Resources | `app/src/main/res/` | Frontend Developer | Drawables, fonts, strings, colors, themes XML |
| App Entry | `app/src/main/kotlin/com/wane/app/WaneApplication.kt`, `MainActivity.kt` | Frontend Developer | Application class, main activity, DI setup |
| Android Services | `app/src/main/kotlin/com/wane/app/service/` | Backend Developer | AccessibilityService (app blocking), NotificationListenerService (notification filtering), ForegroundService (session lifecycle), timer engine |
| Water Animation | `app/src/main/kotlin/com/wane/app/animation/` | Backend Developer | Water rendering engine (OpenGL ES / Canvas), wave physics, caustics, gyroscope response, touch ripples |
| Data / Storage | `app/src/main/kotlin/com/wane/app/data/` | DB Engineer | Room database, DataStore preferences, data models, repositories, DAOs |
| Utilities | `app/src/main/kotlin/com/wane/app/util/` | Backend Developer | Intent helpers, permission utilities, format helpers |
| Build Config | `build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `proguard-rules.pro`, `app/build.gradle.kts` | DevOps | Gradle build, dependencies, signing, ProGuard |
| CI/CD | `.github/`, CI configs | DevOps | GitHub Actions, automated builds, Play Store deployment |
| Unit Tests | `app/src/test/` | Test Engineer | JVM-based unit tests |
| Instrumented Tests | `app/src/androidTest/` | Test Engineer | Device/emulator integration and UI tests |
| Security Reports | `.team/artifacts/security-reviewer/` | Security Reviewer | Security audit reports (no production code) |
| Android Manifest | `app/src/main/AndroidManifest.xml` | Backend Developer | Permissions, service declarations, intent filters |

### File Ownership Verification

No two roles modify the same files:
- **Frontend Developer**: `ui/`, `res/`, `WaneApplication.kt`, `MainActivity.kt`
- **Backend Developer**: `service/`, `animation/`, `util/`, `AndroidManifest.xml`
- **DB Engineer**: `data/`
- **DevOps**: build files, CI configs, gradle
- **Test Engineer**: `test/`, `androidTest/`
- **Security Reviewer**: `.team/artifacts/security-reviewer/` (reports only)

## 3. Approved Tech Stack

> Approved via HIL Gate 2 on 2026-04-13. Full proposals in `.team/proposals/`.

### Android Platform

| Component | Technology | Version | Rationale |
| --------- | ---------- | ------- | --------- |
| Language | Kotlin | 2.3.20 | PROJECT.md constraint + latest stable |
| UI Framework | Jetpack Compose | BOM 2026.03.01 | PROJECT.md constraint + latest stable BOM |
| minSdk | 28 (Android 9) | — | Display cutout for edge-to-edge, foreground service enforcement, 95% device coverage |
| targetSdk | 36 (Android 16) | — | Play Store Aug 2026 deadline; predictive back + adaptive layouts handled from day one |
| compileSdk | 36 (Android 16) | — | Latest stable (since June 2025); best Jetpack library compatibility |
| AGP | Android Gradle Plugin | 9.1.0 | Latest stable |
| Gradle | Gradle Wrapper | 9.4.1 | Required by AGP 9.1.0 |
| JDK Target | 17 | — | Required by AGP 9.x |

### Frontend

| Component | Technology | Version | Rationale |
| --------- | ---------- | ------- | --------- |
| Navigation | Navigation 3 (Nav3) | 1.0.0 | Compose-first, declarative, state-driven; Google's recommendation for new Compose projects |
| State Management | ViewModel + StateFlow + UDF | Lifecycle via BOM | One `StateFlow<UiState>` per screen, sealed `UiEvent`; standard Google architecture |
| DI Framework | Hilt | 2.57.1 (KSP 2.3.20-1.0.30) | Compile-time DI safety; works with Activities, Services, ViewModels |
| Image Loading | None (v1) | — | All assets local (vectors, fonts, GL rendering); Coil 3 earmarked for v1.1 if needed |
| Animation | Compose built-in APIs | via BOM | `spring(stiffness=100f)` maps directly to DESIGN.md specs |
| Architecture | MVVM + UDF | — | State down, events up, one-off effects via `Channel<UiEffect>` |
| Fonts | Bundled TTF | — | Sora (200-600), DM Sans (300-500), Space Grotesk (400-500); ~250KB total |

### Android Services

| Component | Technology | Version | Rationale |
| --------- | ---------- | ------- | --------- |
| Water Rendering | OpenGL ES 3.0 | Platform API | Single-pass GLSL fragment shader via `GLSurfaceView`; ~3-4ms/frame on mid-range GPU |
| Session Management | ForegroundService (`specialUse`) + Coroutines Flow timer | Platform + kotlinx.coroutines | 50ms ticks via `SystemClock.elapsedRealtime()`; survives doze mode |
| App Blocking | AccessibilityService (`TYPE_WINDOW_STATE_CHANGED` only) | Platform API | Narrowest scope; `canRetrieveWindowContent=false`; Play Store compliance protocol |
| Notification Filtering | NotificationListenerService + `snoozeNotification()` | Platform API | Preserves notifications during session; unsnooze on end |
| Gyroscope | `TYPE_GAME_ROTATION_VECTOR` + low-pass filter | Platform API | Hardware sensor fusion; α=0.15; graceful degradation |
| Intent System | Implicit intents (ACTION_DIAL, ACTION_VIEW, ACTION_SENDTO) | Platform API | Zero permissions; OEM-agnostic |
| Emergency Safety | Multi-layer defense (hardcoded immutable constants) | Custom | Never-blocked emergency numbers, always-on repeated caller breakthrough, zero-cooldown exit |
| Water Themes | Data-driven shader uniforms | Custom | Theme switch = update ~20 uniforms; zero frame drops |

### Data / Storage

| Component | Technology | Version | Rationale |
| --------- | ---------- | ------- | --------- |
| Structured Data | Room 3.0 | `androidx.room3` latest stable | Coroutine-first, KSP-only, BundledSQLiteDriver; Room 2.x is maintenance mode |
| Preferences | Jetpack Preferences DataStore | 1.2.x | Flow-based, thread-safe, atomic writes; replaces SharedPreferences |
| Auto-Lock State | In-memory StateFlow | — | Transient; must reset on process death |
| Streaks | Derived from sessions (SQL window functions) | — | No separate table; eliminates sync bugs |
| Encryption | None (Android FBE sufficient) | — | No PII stored; FBE encrypts at-rest when device locked |
| Serialization | kotlinx.serialization | latest stable | Emergency contacts JSON in DataStore |

### Build / Infrastructure

| Component | Technology | Version | Rationale |
| --------- | ---------- | ------- | --------- |
| Build DSL | Kotlin DSL (`build.gradle.kts`) | — | Type-safe, IDE-supported |
| Version Catalog | `gradle/libs.versions.toml` | — | Single source of truth for all dependency versions |
| CI/CD | GitHub Actions (3 workflows) | actions v4 | CI (lint/build/test), Release (tag-triggered AAB), Weekly audit |
| Code Quality | ktlint 1.8.0 + detekt 2.0.0-alpha.2 + Android Lint | — | Pre-commit hook for formatting; detekt alpha required for Kotlin 2.3.x |
| Signing | Env vars (CI) + local.properties (dev) + Play App Signing | — | Keystore Base64 in GitHub Secrets |
| Minification | R8 full mode + custom keep rules | Bundled with AGP | Resource shrinking enabled; keep rules for OpenGL + services |
| Build Variants | Debug + Release (no flavors) | — | No server, no free/paid split |
| Distribution | Play Store via `r0adkll/upload-google-play` | v1 | internal -> closed -> production staged rollout |
| Dependencies | Dependabot (weekly grouped PRs) | — | Grouped by Compose/AndroidX/Kotlin |
| Performance | Baseline Profiles + Macrobenchmark | profileinstaller 1.4.2 | 30-40% startup improvement; frame timing benchmarks |
| App Size Target | < 25MB APK (CI gate) | — | Estimated ~20.5MB; within 30MB PROJECT.md constraint |

### Estimated Battery & Performance

| Component | Frame Budget | Battery Impact |
| --------- | ------------ | -------------- |
| Water shader (GL thread) | ~3-4ms GPU | ~2.5%/hr |
| Sensor processing | ~0.1ms (50Hz) | ~0.3%/hr |
| Session timer | ~0.01ms (50ms ticks) | ~0.1%/hr |
| AccessibilityService | ~0.05ms/event | ~0.2%/hr |
| NotificationListenerService | ~0.05ms/notification | ~0.1%/hr |
| ForegroundService notification | — | ~0.3%/hr |
| **Total** | **~4ms (of 16.6ms budget)** | **~3.5%/hr** (target: <5%) |

## 4. Screen Map

See `.team/artifacts/ui-designer/screen-map.md` for full details. Summary:

| # | Screen | Purpose | Status |
| - | ------ | ------- | ------ |
| 1 | Welcome | Onboarding - first impression | APPROVED |
| 2 | Duration Setup | Onboarding - set initial duration | APPROVED |
| 3 | Auto-Lock Intro | Onboarding - introduce auto-lock | APPROVED |
| 4 | Home | Command center, start sessions | APPROVED |
| 5 | Water Screen | THE product experience (hero) | APPROVED |
| 6 | Emergency Exit | Deliberate friction for early exit | DESIGNED |
| 7 | Session Complete | Gentle re-entry | DESIGNED |
| 14 | Settings Main | All configuration | APPROVED |
| 15 | Auto-Lock Settings | Configure auto-lock details | DESIGNED |
| 8-13 | Basic Phone | Native Android intents (Phone/Contacts/SMS) | NATIVE |

## 5. Team Composition

### Core Roles Active

| # | Role | Status | Phase 3+ Responsibility |
| - | ---- | ------ | ----------------------- |
| 3 | Frontend Developer | ACTIVE | Implement Compose UI, navigation, theme, components |
| 4 | Backend Developer | ACTIVE | Android services (app blocking, notification filtering, session engine), water animation engine, utilities |
| 5 | DB Engineer | ACTIVE | Local storage design (Room/DataStore), data models, repositories |
| 7 | Test Engineer | ACTIVE | Unit tests, instrumented tests, UI tests |
| 8 | Security Reviewer | ACTIVE | Security audit of AccessibilityService usage, data storage, permissions |
| 9 | DevOps | ACTIVE | Gradle build, CI/CD, signing, Play Store, ProGuard |

### Core Roles Completed (Phase 0-2 work done)

| # | Role | Status | Notes |
| - | ---- | ------ | ----- |
| 1 | Content Writer | COMPLETED | All screen content delivered |
| 2 | UI Designer | COMPLETED | All designs approved at HIL Gate 1 |
| 10 | Business/Marketing | COMPLETED | Discovery, personas, value prop delivered |
| 13 | Consent Manager | COMPLETED | Compliance report delivered |

### Core Roles Deferred

| # | Role | Status | Notes |
| - | ---- | ------ | ----- |
| 6 | Data Engineer | DEFERRED | No analytics in v1 (all data local) |
| 11 | User Role | DEFERRED | Activates during Phase 4+ for validation |
| 12 | Customer Specialist | DEFERRED | Not needed during build phases |

### Custom Roles

None required for v1.

## 6. Interface Summary

See [INTERFACES.md](INTERFACES.md) for full contracts. Key integration points:

- **UI Layer** -> **Services Layer**: Start/stop session, query session state, register for session events
- **UI Layer** -> **Data Layer**: Read/write settings, query session history, read streaks
- **Services Layer** -> **Data Layer**: Persist session records on completion, read settings for auto-lock config
- **Services Layer** -> **Animation Engine**: Control water level based on elapsed time, relay gyroscope/touch events
- **Android System** -> **Services Layer**: AccessibilityService callbacks, NotificationListenerService callbacks, screen lock/unlock broadcasts
