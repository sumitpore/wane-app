# Frontend Developer Conventions — Wane

**Created**: 2026-04-13
**Tech Stack**: Kotlin 2.3.20, Compose BOM 2026.03.01, Nav3 1.0.0, Hilt 2.57.1, MVVM + UDF
**Scope**: `app/src/main/kotlin/com/wane/app/ui/`, `app/src/main/res/`, `WaneApplication.kt`, `MainActivity.kt`

> Prerequisites: Read `CONVENTIONS.md` (general), `DESIGN.md` (visual system), and `ui-designer/design-audit.md` before writing any UI code.

---

## 1. File Organization

```
ui/
├── navigation/
│   ├── WaneRoute.kt            -- Sealed interface for all routes
│   └── WaneNavHost.kt          -- Nav3 NavHost setup
├── screens/
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt
│   │   ├── OnboardingViewModel.kt
│   │   └── OnboardingUiState.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── HomeUiState.kt
│   ├── session/
│   │   ├── SessionScreen.kt
│   │   ├── SessionViewModel.kt
│   │   └── SessionUiState.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   ├── SettingsViewModel.kt
│   │   └── SettingsUiState.kt
│   └── autolock/
│       ├── AutoLockSettingsScreen.kt
│       ├── AutoLockViewModel.kt
│       └── AutoLockUiState.kt
├── components/
│   ├── WaneButton.kt           -- Shared button variants
│   ├── DurationPicker.kt
│   ├── ProgressDots.kt
│   ├── SettingsRow.kt
│   ├── BottomToolbar.kt
│   └── ToggleSwitch.kt
└── theme/
    ├── WaneTheme.kt            -- MaterialTheme wrapper
    ├── Color.kt                -- Color definitions
    ├── Type.kt                 -- Typography + FontFamily
    ├── Shape.kt                -- Corner radius definitions
    └── Motion.kt               -- Animation specs
```

**Rules:**
- One screen composable per file. The file is named after the screen: `HomeScreen.kt` contains `@Composable fun HomeScreen()`.
- Shared components go in `ui/components/`. A component is "shared" if used by two or more screens.
- Screen-local helper composables may live in the same file as the screen, below the main composable.
- ViewModel and UiState may share a file for simple screens, but separate files are preferred.

---

## 2. Composable Naming

| Kind | Convention | Example |
| ---- | ---------- | ------- |
| Screen-level | `PascalCase`, matches file name | `@Composable fun HomeScreen(...)` |
| Shared component | `PascalCase`, descriptive noun | `@Composable fun DurationPicker(...)` |
| Screen-internal helper | `PascalCase`, prefixed with screen context if ambiguous | `@Composable fun HomeStreakBadge(...)` |
| Preview | `@Preview fun Preview{ComponentName}()` | `@Preview fun PreviewDurationPicker()` |

**Rules:**
- Every composable function uses `@Composable` annotation on the same line or immediately above the `fun` keyword.
- Never suffix composables with `Composable` (no `HomeScreenComposable`).
- Top-level screen composables accept a ViewModel parameter: `fun HomeScreen(viewModel: HomeViewModel = hiltViewModel())`.

---

## 3. ViewModel & State Naming

| Kind | Pattern | Example |
| ---- | ------- | ------- |
| ViewModel class | `{Screen}ViewModel` | `HomeViewModel`, `SessionViewModel` |
| UiState data class | `{Screen}UiState` | `HomeUiState`, `SessionUiState` |
| Event sealed interface | `{Screen}Event` | `HomeEvent`, `SessionEvent` |
| One-off effect | `{Screen}Effect` | `HomeEffect`, `SessionEffect` |

### State Management Pattern

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.IncreaseDuration -> { /* ... */ }
            is HomeEvent.StartSession -> { /* ... */ }
        }
    }
}
```

**Rules:**
- One `StateFlow<UiState>` per ViewModel. Never expose multiple StateFlows for a single screen.
- State updates use `_uiState.update { it.copy(...) }` — atomic, thread-safe.
- Composables collect state via `val uiState by viewModel.uiState.collectAsStateWithLifecycle()`.
- Events are sealed interface members: `data object` for no-arg events, `data class` for parameterized ones.
- One-off side effects (navigation, toasts) use `Channel<Effect>`, collected in `LaunchedEffect`.
- Never use `LiveData`. All reactive state is `StateFlow` or `Flow`.

---

## 4. Navigation

### Route Definition

```kotlin
sealed interface WaneRoute {
    data object Onboarding : WaneRoute
    data object Home : WaneRoute
    data object Session : WaneRoute
    data object Settings : WaneRoute
    data object AutoLockSettings : WaneRoute
}
```

**Rules:**
- All routes are members of `sealed interface WaneRoute`.
- Route names are PascalCase nouns matching the screen name.
- Emergency Exit and Session Complete are **not** routes — they are composable overlays within `SessionScreen`, animated via Compose animation APIs.
- The back stack is a `SnapshotStateList<WaneRoute>`, owned by `MainActivity` / NavHost.
- Type-safe arguments use data class routes: `data class Session(val durationMs: Long) : WaneRoute`.
- Use `hiltViewModel()` from `androidx.hilt:hilt-navigation-compose` to scope ViewModels per Nav3 destination.

### Predictive Back

- Use `BackHandler` in session screens to intercept the back gesture and show the emergency exit sheet instead of navigating back.
- All other screens use default predictive back behavior (Nav3 handles this).

---

## 5. Theme Usage

**Rules:**
- Always reference theme values via `WaneTheme.colors`, `WaneTheme.typography`, `WaneTheme.shapes`. Never hardcode color hex values, font sizes, or corner radii in composables.
- Color values are defined once in `ui/theme/Color.kt` and exposed through the custom `WaneTheme` object (not Material `MaterialTheme.colorScheme` directly, since Wane uses a custom dark-only palette with white-alpha tokens).
- Typography is defined in `ui/theme/Type.kt` using the bundled Sora, DM Sans, and Space Grotesk font families.
- Corner radii: `12.dp` for sheets and CTAs, `RoundedCornerShape(percent = 50)` for circular elements — defined in `ui/theme/Shape.kt`.

```kotlin
// CORRECT
Text(
    text = "Begin focus session",
    color = WaneTheme.colors.textMuted,
    style = WaneTheme.typography.labelSmall,
)

// WRONG — hardcoded values
Text(
    text = "Begin focus session",
    color = Color.White.copy(alpha = 0.2f),
    fontSize = 14.sp,
)
```

---

## 6. Animation Conventions

### Motion Constants (`ui/theme/Motion.kt`)

All animation specs are defined as top-level constants in `Motion.kt`. Composables reference these constants — never inline `spring()` or `tween()` parameters.

```kotlin
object WaneMotion {
    val SpringDefault: SpringSpec<Float> = spring(
        dampingRatio = 0.7f,
        stiffness = 100f,
    )
    val ScreenFadeIn: TweenSpec<Float> = tween(
        durationMillis = 600,
        easing = EaseOut,
    )
    val SessionEntry: TweenSpec<Float> = tween(
        durationMillis = 800,
        easing = EaseOut,
    )
    val StaggerIntervalHome = 200L   // ms between home element entrances
    val StaggerIntervalSettings = 50L // ms between settings row entrances
    val DotTransition: TweenSpec<Dp> = tween(durationMillis = 300)
    const val PressScaleCircle = 0.95f
    const val PressScaleCta = 0.97f
    const val PressTranslateYDp = -1f
}
```

**Rules:**
- Use `spring()` for interactive elements (panels, sheets, button feedback).
- Use `tween()` for non-interactive transitions (screen fades, progress dots).
- Use `Modifier.graphicsLayer { }` for scale/translate animations — keeps them in the draw phase for GPU acceleration.
- Staggered entrances use `LaunchedEffect` + `delay(interval * index)` + `Animatable.animateTo()`.

---

## 7. Modifier Chaining

**Rules:**
- Chain modifiers in a consistent order: layout → drawing → interaction → semantics.
- Recommended order: `size` / `padding` / `fillMaxWidth` → `background` / `clip` / `border` → `clickable` / `pointerInput` → `semantics` / `testTag`.
- Extract complex modifier chains into extension functions when reused across components.
- Always apply `Modifier` as the first parameter of composable functions, with default `Modifier`:

```kotlin
@Composable
fun WaneButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,  // always first optional param
) { /* ... */ }
```

---

## 8. Accessibility

**Rules:**
- Every interactive element (buttons, toggles, pickers) must have a `contentDescription`. Use `semantics { contentDescription = "..." }` or the `contentDescription` parameter on `Icon` / `Image`.
- Minimum touch target size: **44.dp** for general interactive elements, **56.dp** for toolbar icons. Use `Modifier.sizeIn(minWidth = 44.dp, minHeight = 44.dp)` or wrap smaller visuals in a larger clickable area.
- Decorative elements (dividers, background gradients) use `contentDescription = null` to exclude from accessibility tree.
- Screen reader announcements for state changes: use `LiveRegion.Polite` on the duration display and streak counter so TalkBack announces changes.
- The water animation's `GLSurfaceView` is marked as `importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO` — it is a visual-only element.

---

## 9. Compose Previews

**Rules:**
- Every shared component in `ui/components/` must have at least one `@Preview` function.
- Preview functions are named `@Preview fun Preview{ComponentName}()`.
- Previews must wrap content in `WaneTheme { }` to render with correct colors and typography.
- Use `@PreviewLightDark` only if a light theme is ever added. For now, a single `@Preview` with dark background is sufficient.
- Keep preview data minimal and hardcoded — never inject ViewModels or repositories into previews.

```kotlin
@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
fun PreviewDurationPicker() {
    WaneTheme {
        DurationPicker(
            minutes = 25,
            onIncrease = {},
            onDecrease = {},
        )
    }
}
```

---

## 10. Import Order

Kotlin imports are ordered by the IDE's default sorting (alphabetical), with no manual grouping. Let ktlint enforce this. Do not add blank lines between import groups.

---

## 11. Edge-to-Edge & System Bars

**Rules:**
- The app renders edge-to-edge on all screens. Use `WindowCompat.setDecorFitsSystemWindows(window, false)` in `MainActivity.onCreate()`.
- Handle insets via `Modifier.windowInsetsPadding()` for screens with content behind system bars.
- The water animation screen (`SessionScreen`) renders behind both status bar and navigation bar — the water is full-bleed.
- Status bar icons are always light (white) on all screens (dark-only theme).

---

## 12. String Resources

**Rules:**
- All user-facing text is defined in `res/values/strings.xml`. Never hardcode strings in composables.
- Use `stringResource(R.string.xxx)` in composables.
- String key naming: `screen_element_description` format — e.g., `home_button_start`, `settings_label_duration`, `onboarding_body_step1`.
- Banned words (from CONVENTIONS.md): "addiction", "limit", "block", "detox", "digital", "wellbeing".
