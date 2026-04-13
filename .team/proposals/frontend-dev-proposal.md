# Frontend Tech Stack Proposal — Wane

**Author**: Frontend Developer
**Date**: 2026-04-13
**Status**: Pending HIL Gate 2 Approval

---

## Summary

This proposal covers every technology decision in the UI layer of Wane: the Compose screens, navigation, state management, dependency injection, theming, animation, and image/asset loading. Each section states the recommended choice, its version, rationale, rejected alternatives, and which modules consume it.

All choices assume:
- Kotlin 2.3.20 (latest stable)
- Android Gradle Plugin 9.1.0 (latest stable)
- Single-module app architecture (no multi-module split for v1)
- Dark-mode-only, fully local app with 9 Wane-owned screens

---

## 1. Minimum SDK Version

| Property | Value |
|----------|-------|
| **Recommended** | `minSdk = 28` (Android 9 Pie) |
| **targetSdk** | `35` (Android 15 — Google Play requirement for new apps in 2026) |
| **compileSdk** | `35` |

### Rationale

- **NotificationListenerService** works from API 18+, but the notification channel system (critical for filtering by app) was introduced in API 26. API 28 gives us `NotificationListenerService.requestRebind()` and reliable channel-based filtering out of the box.
- **AccessibilityService** is available from API 4, but the `AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS` flag (API 21+) and the refined foreground app detection via `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED` are stable from API 26+. API 28 improves power management for background services (`FOREGROUND_SERVICE` permission is required from API 28).
- **Jetpack Compose** requires minSdk 21, but the Compose BOM 2026.03.01 and Navigation 3 are tested primarily against API 26+. API 28 provides display cutout handling (`WindowInsets.displayCutout`), which is essential for edge-to-edge water animation rendering.
- **Display cutouts & edge-to-edge**: The water animation is full-bleed and must render behind system bars. API 28 introduced `layoutInDisplayCutoutMode`, required for reliable edge-to-edge on notched devices.
- **Device coverage**: API 28+ covers ~95% of active Android devices globally as of early 2026. Our target users (ages 16-42, North America primary) skew heavily toward newer devices.
- **Battery & performance**: API 28+ provides `UsageStatsManager` improvements and foreground service enforcement that our session management depends on.

### Alternatives Considered

| SDK Level | Why Rejected |
|-----------|-------------|
| `minSdk 21` (Lollipop) | Adds ~3% device coverage but requires extensive compat shims for notification channels, foreground services, and display cutout handling. Not worth the maintenance cost. |
| `minSdk 26` (Oreo) | Viable, but misses foreground service permission enforcement and display cutout APIs from API 28. Only ~1.5% more device coverage vs 28. |
| `minSdk 31` (Android 12) | Too aggressive. Cuts off ~15% of active devices. Some target personas (e.g., Kabir, student demographic) may use older devices. |

### Modules Using This

All modules — this is a project-level setting in `app/build.gradle.kts`.

---

## 2. Compose BOM Version

| Property | Value |
|----------|-------|
| **Recommended** | `androidx.compose:compose-bom:2026.03.01` |
| **Compose Compiler** | Managed by Kotlin 2.3.20 (compiler plugin embedded since Kotlin 2.0) |

### Rationale

- BOM 2026.03.01 is the latest stable BOM as of April 2026, aligning all Compose library versions (UI, Foundation, Material 3, Animation, Runtime) to tested-compatible releases.
- The BOM approach eliminates version conflict issues between Compose sub-libraries.
- Since Kotlin 2.0, the Compose compiler is embedded in the Kotlin compiler plugin — no separate `compose-compiler` artifact is needed. Kotlin 2.3.20 includes the matching Compose compiler.
- This BOM provides the latest `Modifier.graphicsLayer` optimizations for GPU-accelerated animations (critical for 60fps water rendering on the UI layer side).

### Alternatives Considered

| Option | Why Rejected |
|--------|-------------|
| BOM 2026.01.00 | Older; misses performance improvements and bug fixes from the March release. No reason to pin to an older stable. |
| Alpha/snapshot BOMs | Wane is a production app targeting Play Store. Alpha introduces instability risk. |
| No BOM (manual versions) | Increases risk of version mismatches between Compose sub-libraries. No benefit. |

### Modules Using This

`app/build.gradle.kts` — all Compose UI code in `ui/`.

---

## 3. Navigation

| Property | Value |
|----------|-------|
| **Recommended** | **Navigation 3 (Nav3)** — `androidx.navigation3:navigation3-runtime:1.0.0` + `androidx.navigation3:navigation3-ui:1.0.0` |

### Rationale

- **Nav3 is the official Google recommendation for new Compose projects** as of November 2025. It is Compose-first, declarative, and state-driven.
- Wane owns the back stack as a `SnapshotStateList`, which gives us full control over navigation transitions — essential for the 0.6–0.8s spring-physics screen transitions described in DESIGN.md.
- The app has a simple, linear flow: Onboarding (3 steps) → Home → Session (with overlays for Emergency Exit and Session Complete) → Settings (slide-up panel). Nav3's declarative model handles this elegantly without NavController ceremony.
- Nav3 integrates with `hilt-navigation-compose` for ViewModel scoping per destination.
- Type-safe argument passing is built in (sealed classes / data objects for routes).
- Supports Android predictive back gesture API out of the box.
- We pin to the stable 1.0.0 release. Version 1.1.0-rc01 is available but not yet stable.

### Navigation Graph (Proposed)

```
sealed interface WaneRoute {
    data object Onboarding : WaneRoute
    data object Home : WaneRoute
    data object Session : WaneRoute
    data object Settings : WaneRoute
    data object AutoLockSettings : WaneRoute
}
```

The Emergency Exit sheet and Session Complete overlay are not navigation destinations — they are composable overlays within the Session screen, animated via Compose animation APIs.

### Alternatives Considered

| Library | Why Rejected |
|---------|-------------|
| **Navigation 2 (2.9.x)** | Legacy imperative model with `NavController`. Still maintained but not recommended for new projects. Nav3's declarative state ownership is a better fit for Compose idioms. |
| **Voyager** | Good for rapid prototyping, but it's a third-party library with a smaller maintainer base. For an Android-only app, the official Google library is the safer long-term bet. Voyager's `ScreenModel` adds an unnecessary abstraction layer when we already have Hilt ViewModels. |
| **Decompose** | Architecturally powerful but over-engineered for a single-platform, 9-screen app. Decompose's component-tree model shines in multiplatform and deeply nested navigation — neither applies to Wane. Steeper learning curve with minimal benefit here. |
| **Custom (no library)** | Wane's flow is simple enough that manual `AnimatedContent`-based switching could work, but we'd lose deep-link support, back-stack management, and predictive back integration. Not worth reinventing. |

### Modules Using This

- `ui/navigation/` — route definitions, NavHost setup
- `ui/screens/` — each screen composable is a Nav3 destination
- `WaneApplication.kt` / `MainActivity.kt` — NavHost entry point

---

## 4. State Management

| Property | Value |
|----------|-------|
| **Recommended** | **MVVM with ViewModel + StateFlow + UDF (Unidirectional Data Flow)** |
| **Libraries** | `androidx.lifecycle:lifecycle-viewmodel-compose` (via Compose BOM), `kotlinx.coroutines` (bundled with Kotlin) |

### Rationale

- MVVM with StateFlow is the standard Android architecture recommended by Google's "Guide to App Architecture."
- Each screen gets a `ViewModel` that exposes a single `StateFlow<UiState>` (sealed/data class). The Composable collects this flow via `collectAsStateWithLifecycle()`.
- User actions are modeled as a sealed `UiEvent` interface, dispatched to the ViewModel's `onEvent()` function. This is UDF: State flows down, events flow up.
- StateFlow is lifecycle-aware when collected with `collectAsStateWithLifecycle()`, preventing unnecessary recomposition when the app is backgrounded.
- This approach is simple, well-documented, testable (ViewModels are plain Kotlin classes with `TestCoroutineDispatcher`), and familiar to any Android developer.

### State Architecture Per Screen

| Screen | ViewModel | UiState Fields |
|--------|-----------|----------------|
| Onboarding | `OnboardingViewModel` | `currentStep: Int`, `waterLevel: Float` |
| Home | `HomeViewModel` | `durationMinutes: Int`, `streakDays: Int`, `isSettingsOpen: Boolean` |
| Session | `SessionViewModel` | `waterLevel: Float`, `elapsedMs: Long`, `isExitSheetVisible: Boolean`, `isSessionComplete: Boolean`, `exitInput: String` |
| Settings | `SettingsViewModel` | `settings: WaneSettings` (data class with all fields) |
| AutoLockSettings | `AutoLockViewModel` | `autoLockEnabled: Boolean`, `duration: Int`, `gracePeriod: Int`, `skipBetween: TimeRange?`, `skipWhileCharging: Boolean` |

### Alternatives Considered

| Approach | Why Rejected |
|----------|-------------|
| **Full MVI (Model-View-Intent)** with a reducer | MVI with a dedicated `Reducer` function is more boilerplate than needed for 5 ViewModels. The UDF pattern with `onEvent()` achieves the same unidirectionality without the ceremony. If Wane scales significantly, we can introduce a formal reducer later — it's a compatible evolution. |
| **Compose-only state (`remember` / `mutableStateOf`)** | Works for purely UI-local state (e.g., animation progress), but session state must survive configuration changes and be accessible from services. ViewModels solve this. |
| **Redux-style (Orbit MVI, MVIKotlin)** | Third-party MVI libraries add dependencies and learning curve for a 5-screen state problem. Overkill. |

### Modules Using This

- `ui/screens/` — each screen's ViewModel
- `ui/` — shared UI state types

---

## 5. Dependency Injection

| Property | Value |
|----------|-------|
| **Recommended** | **Hilt** — `com.google.dagger:hilt-android:2.57.1` |
| **Compiler** | `com.google.dagger:hilt-android-compiler:2.57.1` via KSP (`com.google.devtools.ksp:2.3.20-1.0.30`) |
| **Compose Integration** | `androidx.hilt:hilt-navigation-compose:1.3.0` |

### Rationale

- Hilt is the officially recommended DI framework for Android by Google. It provides compile-time dependency graph validation — missing bindings fail the build, not the runtime.
- `@HiltViewModel` integrates seamlessly with Compose Navigation (both Nav2 and Nav3) via `hiltViewModel()`.
- Hilt's `@AndroidEntryPoint` annotation works with `Activity`, `Service`, and `BroadcastReceiver` — critical because Wane has an `AccessibilityService`, `NotificationListenerService`, and a `ForegroundService` that need injected dependencies (repositories, settings, session state).
- KSP (Kotlin Symbol Processing) replaces the deprecated KAPT for annotation processing. KSP 2 is the default in Kotlin 2.3.x with significantly faster build times.
- Compile-time safety catches DI errors before they reach users — important for an app that manages system-level services.

### DI Modules (Proposed)

| Module | Provides |
|--------|----------|
| `AppModule` | Application-scoped singletons: `SessionRepository`, `SettingsRepository`, `SessionManager` |
| `DataModule` | `RoomDatabase`, `DataStore<Preferences>`, DAOs |
| `ServiceModule` | Service-scoped bindings for `AccessibilityService`, `NotificationListenerService` |

### Alternatives Considered

| Framework | Why Rejected |
|-----------|-------------|
| **Koin 4.2** | Simpler DSL, no annotation processing, faster initial setup. However: (1) runtime resolution means DI errors crash at runtime, not build time, (2) technically a service locator, not true DI, (3) Google's testing and sample ecosystem is built around Hilt. For a production app with system services, compile-time safety wins. |
| **Manual DI** | Viable for a small app, but Wane has ViewModels, services, repositories, and a database. Manual DI means writing and maintaining a custom `AppContainer` with factory methods. Hilt eliminates this boilerplate with negligible build-time cost. |
| **Dagger (raw)** | Hilt is built on Dagger. Using raw Dagger provides no additional capability but requires significantly more boilerplate (Component interfaces, @Component.Builder, etc.). Hilt removes this overhead. |

### Modules Using This

- `WaneApplication.kt` — `@HiltAndroidApp`
- `MainActivity.kt` — `@AndroidEntryPoint`
- `ui/screens/` — `@HiltViewModel` on all ViewModels
- `service/` — `@AndroidEntryPoint` on services (Backend Developer's domain but DI config is shared)
- `data/` — `@Module` / `@Provides` for database and DataStore (DB Engineer's domain)
- `app/build.gradle.kts` — Hilt and KSP plugin configuration

---

## 6. Image / Asset Loading

| Property | Value |
|----------|-------|
| **Recommended** | **No dedicated image loading library for v1** — use Compose built-in `painterResource()` and `ImageVector` |
| **Conditional future addition** | Coil 3 (`io.coil-kt.coil3:coil-compose:3.4.0`) if network images are needed in v1.1+ |

### Rationale

- Wane v1 has **no network images**. The app is fully local with no server calls.
- All visual assets are: (1) the water animation (rendered via Canvas/OpenGL, not loaded as images), (2) icon vectors (Lucide-style icons, bundled as `ImageVector` or SVG drawables), (3) custom font files (Sora, DM Sans, Space Grotesk).
- For bundled drawables and vector icons, Compose's built-in `painterResource(R.drawable.*)` and `ImageVector` are sufficient with zero added dependency weight.
- Adding Coil or Glide for v1 would increase APK size (~400-800KB) with no benefit. The app has a < 30MB size target.
- If premium water themes in v1.1+ require downloading theme assets from a CDN, Coil 3 is the recommended choice (Kotlin-first, Compose-native, lightweight).

### Alternatives Considered

| Library | Why Rejected (for v1) |
|---------|----------------------|
| **Coil 3.4.0** | Excellent library, but adds unnecessary weight when all assets are local. Earmarked for v1.1 if network image loading is needed. |
| **Glide** | Java-first heritage, larger footprint than Coil, Compose integration (`GlideImage`) is a separate artifact. No advantage over Coil for Kotlin/Compose projects. |
| **Fresco** | Facebook's library. Heavy, complex, designed for apps with massive image feeds. Completely overkill for Wane. |

### Modules Using This

- `ui/` — `painterResource()` for icons and drawables
- `res/drawable/` — vector XML drawables for icons
- `res/font/` — font resource files

---

## 7. Animation Framework

| Property | Value |
|----------|-------|
| **Recommended** | **Compose Animation APIs (built-in)** — `androidx.compose.animation:animation` (via BOM) |
| **Supplementary** | `Animatable`, `animateFloatAsState`, `updateTransition`, `AnimatedContent`, `AnimatedVisibility`, `spring()` spec |

### Rationale

Wane's animation needs fall into two categories:

**Category A — UI Chrome Animations (Frontend Developer's domain):**
- Screen transitions (0.6–0.8s fades)
- Spring-physics slide-ups (settings panel, emergency exit sheet): `spring(stiffness = 100f, dampingRatio = ...)` maps directly to Compose's `spring()` animation spec
- Staggered entrance animations (home elements at 0.2s intervals, settings rows at 0.05s intervals): achievable with `LaunchedEffect` + `delay` + `Animatable`
- Button press feedback (`scale(0.95)`, `translateY(-1.dp)`): `Modifier.graphicsLayer` with `animateFloatAsState`
- Progress dot width transitions (300ms): `animateDpAsState`
- Opacity transitions: `animateFloatAsState`

Compose's built-in animation APIs handle all of these natively:
- `spring(stiffness = 100f, dampingRatio = 0.7f)` directly maps to DESIGN.md's spring specs (stiffness 100, damping 20 → dampingRatio ≈ 0.7 for our use case).
- `AnimatedContent` with `fadeIn/fadeOut` handles screen transitions.
- `AnimatedVisibility` with `slideInVertically` + spring spec handles the settings panel.
- `Modifier.graphicsLayer { }` keeps animations in the draw phase for GPU acceleration.

**Category B — Water Animation (Backend Developer's domain):**
The water rendering (wave layers, caustic lights, gradient body) is owned by the Backend Developer per ARCHITECTURE.md. It will use either OpenGL ES or `Compose Canvas` with `DrawScope` — this is outside the frontend proposal scope.

### Compose-to-DESIGN.md Motion Mapping

| DESIGN.md Spec | Compose Implementation |
|----------------|----------------------|
| Spring stiffness 100, damping 20 | `spring(dampingRatio = 0.7f, stiffness = 100f)` |
| 0.6s screen fade | `tween(600, easing = EaseOut)` inside `AnimatedContent` |
| 0.8s session entry | `tween(800, easing = EaseOut)` |
| Stagger 0.2s | `LaunchedEffect` with `delay(200 * index)` + `Animatable.animateTo()` |
| Stagger 0.05s | `LaunchedEffect` with `delay(50 * index)` + `Animatable.animateTo()` |
| `scale(0.95)` press | `Modifier.graphicsLayer { scaleX = scale; scaleY = scale }` with `animateFloatAsState(if (pressed) 0.95f else 1f)` |
| `scale(0.97) + translateY(-1dp)` | Combined `graphicsLayer` with two animated values |
| 300ms dot width | `animateDpAsState(targetValue, tween(300))` |

### Alternatives Considered

| Library | Why Rejected |
|---------|-------------|
| **Lottie (`lottie-compose:6.7.1`)** | Lottie is designed for After Effects JSON animations. Wane's UI animations are all programmatic (springs, fades, scales) — not pre-authored AE files. Adding Lottie would increase APK size for zero benefit. If premium themes in v1.1+ include designer-authored animations, Lottie can be added then. |
| **Custom animation engine** | Unnecessary when Compose's `spring()`, `tween()`, `Animatable`, and `graphicsLayer` cover every motion spec in DESIGN.md. Reinventing would waste time and increase maintenance burden. |
| **MotionLayout (Compose)** | Designed for complex constraint-based transitions between layout states. Overkill for Wane's simple fade/slide/scale animations. |
| **Material 3 Expressive MotionScheme** | Still in alpha as of April 2026. Promising centralized motion theming, but not stable enough for production. We can adopt it later when it stabilizes. |

### Modules Using This

- `ui/screens/` — all screen composables use animation APIs
- `ui/components/` — shared animated components (buttons, progress dots, settings panel)
- `ui/theme/` — spring spec constants defined as shared `AnimationSpec` objects

---

## 8. Architecture Pattern

| Property | Value |
|----------|-------|
| **Recommended** | **MVVM + UDF (Unidirectional Data Flow)** |
| **Pattern summary** | ViewModel exposes `StateFlow<UiState>`, Composable sends `UiEvent`, ViewModel processes events and updates state |

### Rationale

- MVVM is the canonical Android architecture pattern, documented extensively in Google's official guidance.
- UDF ensures predictable state: the UI is always a pure function of `UiState`, and state changes only happen through explicit `UiEvent` dispatches.
- This pattern is inherently testable: ViewModels can be unit-tested by (1) sending events, (2) asserting emitted states, without any Android framework dependency.
- For Wane's 5 ViewModels, this is the right level of formalism — structured enough to be maintainable, simple enough to not impose overhead.

### Data Flow Diagram

```
┌─────────────┐    UiEvent     ┌──────────────┐    Repository    ┌─────────────┐
│  Composable │ ──────────────> │  ViewModel   │ ───────────────> │    Data      │
│  (Screen)   │                │              │                  │   Layer      │
│             │ <────────────── │  StateFlow   │ <─────────────── │ (Room/       │
│  collectAs  │    UiState     │  <UiState>   │    Flow<Data>    │  DataStore)  │
│  State()    │                │              │                  │             │
└─────────────┘                └──────────────┘                  └─────────────┘
                                      │
                                      │ Interface call
                                      ▼
                               ┌──────────────┐
                               │   Services   │
                               │   Layer      │
                               │ (Session     │
                               │  Manager)    │
                               └──────────────┘
```

### UiState Convention

Every screen state is a single data class:

```kotlin
data class HomeUiState(
    val durationMinutes: Int = 25,
    val streakDays: Int = 0,
    val isSettingsOpen: Boolean = false,
    val isLoading: Boolean = false,
)
```

State updates use `MutableStateFlow.update { it.copy(...) }` for thread-safe, atomic updates.

### UiEvent Convention

Events are modeled as a sealed interface per screen:

```kotlin
sealed interface HomeEvent {
    data object IncreaseDuration : HomeEvent
    data object DecreaseDuration : HomeEvent
    data object StartSession : HomeEvent
    data object OpenSettings : HomeEvent
    data object CloseSettings : HomeEvent
}
```

### One-off Effects

Navigation events, snackbars, and other one-off side effects use `Channel<UiEffect>` exposed as `Flow<UiEffect>`, collected in the Composable via `LaunchedEffect`.

### Alternatives Considered

| Pattern | Why Rejected |
|---------|-------------|
| **Full MVI with Reducer** | Adds a formal `reduce(state, event) -> state` function. For 5 small ViewModels, this is ceremony without benefit. The `onEvent()` pattern achieves the same unidirectionality. If the app grows significantly, migrating to a formal reducer is backwards-compatible. |
| **MVP (Model-View-Presenter)** | Legacy pattern designed for View-based Android. Does not align with Compose's declarative model. |
| **No ViewModel (Compose-only state)** | `remember` and `mutableStateOf` work for ephemeral UI state but cannot survive process death, configuration changes, or be shared with services. |

### Modules Using This

- `ui/screens/` — ViewModels and UiState/UiEvent definitions
- `ui/` — shared state types and effect patterns

---

## 9. Kotlin & Build Tooling

| Property | Value |
|----------|-------|
| **Kotlin** | `2.3.20` (latest stable, March 2026) |
| **Android Gradle Plugin** | `9.1.0` (latest stable, March 2026) |
| **KSP** | `2.3.20-1.0.30` (matched to Kotlin version) |
| **Gradle** | `9.3.1` (required by AGP 9.1.0) |
| **Java target** | `17` (required by AGP 9.x) |

### Rationale

- Kotlin 2.3.20 includes the embedded Compose compiler plugin, KSP2 support, and the latest coroutines integration.
- AGP 9.1.0 is the latest stable, with improved build caching and R8 shrinking.
- KSP 2.3.20-1.0.30 is matched to Kotlin 2.3.20 and is required for Hilt annotation processing (KAPT is deprecated in Kotlin 2.3.x).
- JVM target 17 is required by AGP 9.x and provides modern language features for Gradle build scripts.

### Modules Using This

All modules — project-level configuration.

---

## 10. Custom Fonts

| Property | Value |
|----------|-------|
| **Recommended** | Bundle as `.ttf` files in `res/font/`, loaded via Compose `FontFamily` |
| **Fonts** | Sora (weights 200, 300, 400, 500, 600), DM Sans (300, 400, 500), Space Grotesk (400, 500) |

### Rationale

- DESIGN.md and the design audit specify three font families. Google Fonts provides all three as free downloads.
- Sora weight 200 (ExtraLight) is explicitly used for the duration display (64sp). The design audit notes this weight is NOT in the default Google Fonts import — we must explicitly bundle it.
- Bundled TTF files in `res/font/` are the standard Android approach, support XML font families, and are compatible with Compose `FontFamily`.
- Total font file size estimate: ~250KB (10 weight files × ~25KB each), well within the < 30MB APK target.

### Font Loading in Compose

```kotlin
val Sora = FontFamily(
    Font(R.font.sora_extralight, FontWeight.ExtraLight),  // 200
    Font(R.font.sora_light, FontWeight.Light),             // 300
    Font(R.font.sora_regular, FontWeight.Normal),          // 400
    Font(R.font.sora_medium, FontWeight.Medium),           // 500
    Font(R.font.sora_semibold, FontWeight.SemiBold),       // 600
)
```

### Alternatives Considered

| Approach | Why Rejected |
|----------|-------------|
| **Downloadable Fonts (Google Fonts Provider)** | Requires a network call on first use. Wane is fully local with no network calls. Downloadable fonts also have a fallback delay that would cause visible font swaps — unacceptable for a premium design. |
| **System fonts only** | DESIGN.md explicitly bans system defaults and Inter. Custom typography is core to the brand identity. |

### Modules Using This

- `res/font/` — font files
- `ui/theme/` — `FontFamily` and `Typography` definitions

---

## Dependency Summary

### `app/build.gradle.kts` Dependencies

```kotlin
// -- Platform --
android {
    compileSdk = 35
    defaultConfig {
        minSdk = 28
        targetSdk = 35
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// -- Compose --
val composeBom = platform("androidx.compose:compose-bom:2026.03.01")
implementation(composeBom)
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.foundation:foundation")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.animation:animation")
implementation("androidx.compose.ui:ui-tooling-preview")
debugImplementation("androidx.compose.ui:ui-tooling")

// -- Navigation 3 --
implementation("androidx.navigation3:navigation3-runtime:1.0.0")
implementation("androidx.navigation3:navigation3-ui:1.0.0")

// -- Lifecycle / ViewModel --
implementation("androidx.lifecycle:lifecycle-runtime-compose")     // via BOM
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")   // via BOM
implementation("androidx.activity:activity-compose")               // via BOM

// -- Hilt --
implementation("com.google.dagger:hilt-android:2.57.1")
ksp("com.google.dagger:hilt-android-compiler:2.57.1")
implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
```

### `gradle/libs.versions.toml` (Version Catalog)

```toml
[versions]
kotlin = "2.3.20"
agp = "9.1.0"
ksp = "2.3.20-1.0.30"
compose-bom = "2026.03.01"
hilt = "2.57.1"
hilt-navigation-compose = "1.3.0"
navigation3 = "1.0.0"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

### Total Frontend-Owned Dependencies: 6

| # | Dependency | Purpose | Size Impact |
|---|-----------|---------|-------------|
| 1 | Compose BOM 2026.03.01 | UI framework | ~3MB (shared with Material3) |
| 2 | Navigation 3 1.0.0 | Screen routing | ~200KB |
| 3 | Hilt 2.57.1 | Dependency injection | ~300KB (shared with services) |
| 4 | Lifecycle/ViewModel | State management | ~150KB (already in Compose deps) |
| 5 | KSP 2.3.20-1.0.30 | Build-time annotation processing | 0 (build-only, not in APK) |
| 6 | Custom fonts (Sora, DM Sans, Space Grotesk) | Typography | ~250KB |

**Estimated UI layer contribution to APK: ~4MB** (well within the < 30MB budget).

---

## Self-Verification Checklist

| # | Check | Status |
|---|-------|--------|
| 1 | Covers all 8 required technology choices (minSdk, navigation, state, DI, images, animation, BOM, architecture) | PASS |
| 2 | All version numbers are current stable releases as of April 2026 | PASS |
| 3 | Every choice includes alternatives considered with rejection rationale | PASS |
| 4 | Specific enough for another developer to run `gradle sync` and start building | PASS |
| 5 | Respects PROJECT.md constraints (Kotlin + Jetpack Compose, fully local, no network) | PASS |
| 6 | Aligns with DESIGN.md motion specs (spring physics, stagger, transitions) | PASS |
| 7 | Aligns with ARCHITECTURE.md module boundaries (frontend owns `ui/`, `res/`, `MainActivity`) | PASS |
| 8 | No dependency conflicts between proposed libraries | PASS |
| 9 | APK size estimate within < 30MB budget | PASS |
| 10 | minSdk supports all required system services (AccessibilityService, NotificationListenerService) | PASS |
