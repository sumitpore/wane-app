# Interface Contracts: Wane

**Created**: 2026-04-10
**Last Updated**: 2026-04-13
**Status**: Approved (post HIL Gate 2)

> These contracts define how modules communicate. Every module MUST implement its side of each contract. A developer can implement any module by reading only this file plus their own proposal.

**Package root**: `com.wane.app`

---

## 1. Shared Types

All shared types live in `com.wane.app.shared`. Both UI, Services, and Data layers depend on this package. No shared type may import from `ui/`, `service/`, `animation/`, or `data/`.

### 1.1 SessionState

Finite state machine for focus session lifecycle. Produced by Services (`SessionManager`), consumed by UI (`SessionViewModel`) and Services (`AccessibilityService`, `NotificationListener`).

```kotlin
package com.wane.app.shared

sealed class SessionState {

    data object Idle : SessionState()

    data class Running(
        val sessionId: Long,
        val totalDurationMs: Long,
        val remainingMs: Long,
        val waterLevel: Float       // 1.0 (full) → 0.0 (empty)
    ) : SessionState()

    data class Completing(
        val sessionId: Long,
        val totalDurationMs: Long,
        val actualDurationMs: Long
    ) : SessionState()

    data object EmergencyExit : SessionState()
}
```

**Valid transitions:**

| From | To | Trigger |
| ---- | -- | ------- |
| `Idle` | `Running` | `SessionManager.startSession()` |
| `Running` | `Completing` | Timer reaches 0 (water fully drained) |
| `Running` | `EmergencyExit` | User confirms emergency exit ("EXIT" typed) |
| `Completing` | `Idle` | User taps "Done" on completion screen |
| `EmergencyExit` | `Idle` | Immediate (session record persisted, services reset) |

**Invariants:**
- `waterLevel = remainingMs.toFloat() / totalDurationMs.toFloat()` — always derived, never set independently
- `remainingMs` is monotonically decreasing while `Running`, ticked at 50ms intervals using `SystemClock.elapsedRealtime()`
- `sessionId` is assigned by `SessionRepository.recordSession()` at session start (insert with `endTime=0`, update on completion)

---

### 1.2 FocusSession

Room entity for session history. Owned by Data layer, written by Services, read by UI.

```kotlin
package com.wane.app.shared

data class FocusSession(
    val id: Long = 0,               // Room auto-generated PK
    val startTime: Long,            // epoch millis (System.currentTimeMillis())
    val endTime: Long,              // epoch millis; 0 while session is active
    val plannedDurationMs: Long,    // user-selected duration in ms
    val actualDurationMs: Long,     // wall-clock time between start and end
    val completionStatus: CompletionStatus,
    val themeId: String             // references WaterTheme.id
)
```

**Notes:**
- Times are UTC epoch millis. Formatting to local time is a UI concern.
- `themeId` is a soft reference (no Room `@ForeignKey`). If theme is deleted, session records survive.
- The Room `@Entity` annotation lives in the Data layer's implementation. The shared type is annotation-free so the UI layer doesn't need Room as a dependency.

---

### 1.3 CompletionStatus

```kotlin
package com.wane.app.shared

enum class CompletionStatus {
    COMPLETED,    // water fully drained, user saw completion screen
    EARLY_EXIT    // user exited via emergency exit flow
}
```

Stored as enum name string in Room via `@TypeConverter`. Two values only — no judgment-laden statuses.

---

### 1.4 WaterTheme

Ownership record for water themes. Stored in Room by Data layer, queried by UI for theme picker, queried by Services to resolve active theme visuals.

```kotlin
package com.wane.app.shared

data class WaterTheme(
    val id: String,                 // "default", "monsoon", "glacier", "koi", "bioluminescence"
    val displayName: String,        // user-facing name
    val isPurchased: Boolean,       // true for free themes, true after IAP
    val purchaseToken: String?,     // Google Play opaque token; null for free themes
    val purchaseTimestamp: Long?    // epoch millis of purchase; null for free themes
)
```

---

### 1.5 WaterThemeVisuals

Shader uniform parameters for the water animation engine. Compile-time constants defined in the Services/Animation layer. The UI layer never reads these directly — it only needs `WaterTheme` for the theme picker. Services resolves `themeId → WaterThemeVisuals` via `WaterThemeCatalog`.

```kotlin
package com.wane.app.shared

data class WaterThemeVisuals(
    val themeId: String,            // matches WaterTheme.id
    // Wave layers (3)
    val wave1: WaveParams,
    val wave2: WaveParams,
    val wave3: WaveParams,
    // Water body gradient (4 stops, top to bottom)
    val gradientTop: Long,          // ARGB color as Long (0xAARRGGBB)
    val gradientUpper: Long,
    val gradientLower: Long,
    val gradientBottom: Long,
    // Caustic lights
    val causticCenterColor: Long,   // ARGB
    val causticCount: Int,          // number of caustic lights (default: 5)
    val causticBaseRadius: Float,   // normalized radius (0.0–1.0)
    val causticRadiusOscillation: Float,
    // Background gradient (above water line)
    val backgroundStart: Long,      // ARGB
    val backgroundEnd: Long         // ARGB
)

data class WaveParams(
    val color: Long,                // ARGB
    val amplitude: Float,           // normalized (0.0–1.0)
    val frequency: Float,           // wave count across screen width
    val speed: Float                // wave animation speed multiplier
)
```

**Note:** Colors are `Long` (not Compose `Color`) so this type has no Compose dependency. Conversion to `Color` or GLSL `vec4` happens at the consumption site.

---

### 1.6 AutoLockConfig

Configuration for auto-lock feature. Persisted in Preferences DataStore, read by Services (`AutoLockScheduler`).

```kotlin
package com.wane.app.shared

data class AutoLockConfig(
    val enabled: Boolean = false,
    val durationMinutes: Int = 30,
    val gracePeriodSeconds: Int = 10,
    val skipStartHour: Int? = null,     // 0–23; null = no skip window
    val skipStartMinute: Int? = null,   // 0–59
    val skipEndHour: Int? = null,       // 0–23
    val skipEndMinute: Int? = null,     // 0–59
    val skipWhileCharging: Boolean = false
)
```

**Validation rules (enforced by PreferencesRepository on write):**
- `durationMinutes` in `5..120`
- `gracePeriodSeconds` in `5..60`
- If any `skip*` field is non-null, all four must be non-null
- `skipStartHour/Minute` != `skipEndHour/Minute` (zero-length window is invalid)

---

### 1.7 TiltState

Gyroscope tilt data from sensor processing. Produced by `TiltSensorManager` (Services), consumed by `WaterCanvas` (Animation/UI).

```kotlin
package com.wane.app.shared

sealed class TiltState {
    data object Unavailable : TiltState()
    data class Available(
        val tiltX: Float,   // low-pass filtered; range approx -1.0 to 1.0
        val tiltY: Float    // low-pass filtered; range approx -1.0 to 1.0
    ) : TiltState()
}
```

If `Unavailable`, the water animation renders without tilt response (static offset `0, 0`). No error — graceful degradation.

---

### 1.8 WaneRoute

Navigation destinations for Nav3. Defined in the shared package so Services can deep-link back to specific screens (e.g., `AccessibilityService` redirects to `Session`).

```kotlin
package com.wane.app.shared

sealed interface WaneRoute {
    data object Onboarding : WaneRoute
    data object Home : WaneRoute
    data object Session : WaneRoute
    data object Settings : WaneRoute
    data object AutoLockSettings : WaneRoute
}
```

**Not navigation destinations** (composable overlays within `Session` screen):
- Emergency Exit sheet — animated overlay, controlled by `SessionViewModel.isExitSheetVisible`
- Session Complete overlay — animated overlay, controlled by `SessionState.Completing`

---

### 1.9 StreakInfo

Display-ready streak data. Computed by Data layer, consumed by UI.

```kotlin
package com.wane.app.shared

data class StreakInfo(
    val currentStreak: Int,         // consecutive days with ≥1 completed session
    val longestStreak: Int,         // all-time longest
    val totalSessions: Int,         // lifetime count
    val totalMinutes: Long          // lifetime focus minutes
)
```

---

## 2. API Contracts

### 2.1 UI → Services: SessionManager

**Location**: `com.wane.app.service.SessionManager`
**DI scope**: `@Singleton` (application-scoped, injected via Hilt)
**Consumers**: `SessionViewModel`, `HomeViewModel`

| # | Signature | Description |
| - | --------- | ----------- |
| 1 | `val sessionState: StateFlow<SessionState>` | Current session state. Initial value: `SessionState.Idle`. UI collects via `collectAsStateWithLifecycle()`. |
| 2 | `suspend fun startSession(durationMs: Long, themeId: String)` | Begin a focus session. Creates a `FocusSession` record, transitions to `Running`, starts foreground service, timer, sensor, and blocking services. |
| 3 | `fun requestEmergencyExit()` | Initiate emergency exit flow. Transitions to `EmergencyExit`, persists session with `EARLY_EXIT`, stops all services. Returns immediately (fire-and-forget). |
| 4 | `fun confirmSessionComplete()` | User tapped "Done" on completion screen. Transitions `Completing → Idle`, stops foreground service. |

**Error cases:**

| Error | Handling |
| ----- | -------- |
| `startSession` called while `Running` | No-op. Log warning. Only one session at a time. |
| `startSession` with `durationMs <= 0` | Throw `IllegalArgumentException`. Caller must validate. |
| `startSession` with unknown `themeId` | Fall back to `"default"` theme. Log warning. |
| `requestEmergencyExit` called while `Idle` | No-op. |
| Foreground service fails to start (Android 12+ background start restrictions) | `SessionManager` catches `ForegroundServiceStartNotAllowedException`, emits `Idle`, surfaces error via `errorEvents: Flow<SessionError>`. |

**SessionError** (one-off events consumed by UI):

```kotlin
sealed class SessionError {
    data object ForegroundServiceBlocked : SessionError()
    data object AccessibilityServiceDisabled : SessionError()
}
```

Exposed as: `val errorEvents: Flow<SessionError>` (backed by `Channel<SessionError>(Channel.BUFFERED)`)

---

### 2.2 UI → Data: SessionRepository

**Location**: `com.wane.app.data.repository.SessionRepository` (interface)
**Implementation**: `com.wane.app.data.repository.impl.SessionRepositoryImpl`
**DI scope**: `@Singleton`
**Consumers**: `HomeViewModel`, `SessionViewModel`, `SettingsViewModel`

| # | Signature | Description |
| - | --------- | ----------- |
| 1 | `fun observeAllSessions(): Flow<List<FocusSession>>` | All sessions, newest first. Room emits on every table change. |
| 2 | `fun observeRecentSessions(limit: Int): Flow<List<FocusSession>>` | Last N sessions, newest first. |
| 3 | `fun observeCurrentStreak(): Flow<Int>` | Current consecutive-day streak. Re-emits when `focus_sessions` table changes. Returns 0 if no sessions. |
| 4 | `fun observeStreakInfo(): Flow<StreakInfo>` | Full streak + aggregate stats. Re-emits on table changes. |
| 5 | `suspend fun recordSession(session: FocusSession): Long` | Insert or update a session record. Returns the row ID. |
| 6 | `suspend fun updateSessionEnd(sessionId: Long, endTime: Long, actualDurationMs: Long, status: CompletionStatus)` | Update an in-progress session on completion or early exit. |
| 7 | `suspend fun clearAllSessions()` | Delete all session history. Used by "Clear Data" in settings. |

**Error cases:**

| Error | Handling |
| ----- | -------- |
| `observeRecentSessions(limit <= 0)` | Throw `IllegalArgumentException` |
| `updateSessionEnd` with non-existent `sessionId` | No-op (0 rows updated). Log warning. |
| Room I/O failure | Propagates `SQLiteException` up. ViewModel catches and maps to UI error state. |

---

### 2.3 UI → Data: PreferencesRepository

**Location**: `com.wane.app.data.repository.PreferencesRepository` (interface)
**Implementation**: `com.wane.app.data.repository.impl.PreferencesRepositoryImpl`
**DI scope**: `@Singleton`
**Consumers**: `HomeViewModel`, `SettingsViewModel`, `AutoLockViewModel`, `OnboardingViewModel`, `SessionManager`, `AutoLockScheduler`

#### Read Operations (Flow-based, reactive)

| # | Signature | Default | Description |
| - | --------- | ------- | ----------- |
| 1 | `fun observeDefaultDuration(): Flow<Int>` | `25` | Default session duration in minutes. |
| 2 | `fun observeAutoLockConfig(): Flow<AutoLockConfig>` | `AutoLockConfig()` | Full auto-lock configuration. |
| 3 | `fun observeSelectedThemeId(): Flow<String>` | `"default"` | Active theme ID. |
| 4 | `fun observeEmergencyContacts(): Flow<List<String>>` | `emptyList()` | Phone numbers. Serialized as JSON in DataStore. |
| 5 | `fun observeAmbientSoundsEnabled(): Flow<Boolean>` | `false` | Ambient sounds toggle. |
| 6 | `fun observeHapticFeedbackEnabled(): Flow<Boolean>` | `true` | Haptic feedback toggle. |
| 7 | `fun observeOnboardingCompleted(): Flow<Boolean>` | `false` | Whether onboarding flow is done. |

#### Write Operations (suspend, atomic)

| # | Signature | Validation | Description |
| - | --------- | ---------- | ----------- |
| 1 | `suspend fun setDefaultDuration(minutes: Int)` | `minutes in 5..120` | Update default session duration. |
| 2 | `suspend fun setAutoLockConfig(config: AutoLockConfig)` | See §1.6 | Update full auto-lock config atomically. |
| 3 | `suspend fun setSelectedThemeId(themeId: String)` | Non-blank | Update active theme. |
| 4 | `suspend fun setEmergencyContacts(contacts: List<String>)` | Each non-blank | Update emergency contacts list. |
| 5 | `suspend fun setAmbientSoundsEnabled(enabled: Boolean)` | — | Toggle ambient sounds. |
| 6 | `suspend fun setHapticFeedbackEnabled(enabled: Boolean)` | — | Toggle haptic feedback. |
| 7 | `suspend fun setOnboardingCompleted(completed: Boolean)` | — | Mark onboarding as done. |

**Error cases:**

| Error | Handling |
| ----- | -------- |
| Validation failure on write | Throw `IllegalArgumentException` with descriptive message. |
| DataStore corruption | DataStore handles internally (re-creates file). Repository emits defaults. |

---

### 2.4 UI → Data: ThemeRepository

**Location**: `com.wane.app.data.repository.ThemeRepository` (interface)
**Implementation**: `com.wane.app.data.repository.impl.ThemeRepositoryImpl`
**DI scope**: `@Singleton`
**Consumers**: `SettingsViewModel` (theme picker), `SessionManager` (resolve active theme)

| # | Signature | Description |
| - | --------- | ----------- |
| 1 | `fun observeAllThemes(): Flow<List<WaterTheme>>` | All themes (free + premium, purchased + locked). |
| 2 | `fun observePurchasedThemes(): Flow<List<WaterTheme>>` | Only themes the user owns. |
| 3 | `suspend fun getThemeById(id: String): WaterTheme?` | Single theme lookup. Returns `null` if not found. |
| 4 | `suspend fun markThemePurchased(themeId: String, purchaseToken: String)` | Record a successful IAP purchase. |
| 5 | `suspend fun seedDefaultThemes()` | Insert default theme(s) on first install. Idempotent (uses `UPSERT`). |

**Error cases:**

| Error | Handling |
| ----- | -------- |
| `getThemeById` with unknown ID | Returns `null`. Caller must handle (fall back to default). |
| `markThemePurchased` with unknown `themeId` | No-op (0 rows updated). Log warning. |

---

### 2.5 Services → Data

Services layer reads from repositories but does not own them. All calls go through the same repository interfaces defined above.

| Service | Repository Method | Purpose |
| ------- | ----------------- | ------- |
| `SessionManager` | `sessionRepo.recordSession(session)` | Persist session on start (endTime=0). |
| `SessionManager` | `sessionRepo.updateSessionEnd(...)` | Update session on completion or exit. |
| `SessionManager` | `themeRepo.getThemeById(id)` | Validate theme exists before session. |
| `SessionManager` | `prefsRepo.observeSelectedThemeId()` | Know which theme to use. |
| `AutoLockScheduler` | `prefsRepo.observeAutoLockConfig()` | React to config changes in real time. |
| `WaneNotificationListener` | `prefsRepo.observeEmergencyContacts()` | Know which callers bypass filtering. |
| `WaneSessionService` | `prefsRepo.observeHapticFeedbackEnabled()` | Whether to trigger haptic on session events. |

---

### 2.6 Services → Animation: WaterCanvas Composable

**Location**: `com.wane.app.animation.WaterCanvas`
**Owner**: Backend Developer (Animation module)
**Consumer**: Frontend Developer (embedded in `SessionScreen`)

```kotlin
@Composable
fun WaterCanvas(
    waterLevel: Float,
    tiltState: TiltState,
    themeVisuals: WaterThemeVisuals,
    onTouch: ((x: Float, y: Float) -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

| Parameter | Type | Description |
| --------- | ---- | ----------- |
| `waterLevel` | `Float` | 0.0 (empty) to 1.0 (full). Drives the vertical fill of the water body. |
| `tiltState` | `TiltState` | Current device tilt. `Unavailable` → no tilt effect. |
| `themeVisuals` | `WaterThemeVisuals` | Active theme's shader parameters. Changing this swaps all uniforms — no recompilation. |
| `onTouch` | `((Float, Float) -> Unit)?` | Optional touch callback for ripple effects. Coordinates are normalized (0.0–1.0). `null` disables touch ripples. |
| `modifier` | `Modifier` | Standard Compose modifier. Typically `Modifier.fillMaxSize()`. |

**Implementation details:**
- Internally hosts a `GLSurfaceView` via `AndroidView`
- Renders on its own GL thread (does not block Compose UI thread)
- Reads `waterLevel` and `tiltState` via atomic floats for lock-free GL thread access
- Renders at 60fps (`RENDERMODE_CONTINUOUSLY`); drops to 30fps when battery < 15%

**Error cases:**

| Condition | Behavior |
| --------- | -------- |
| OpenGL ES 3.0 unavailable | Falls back to `GLES20` (ES 2.0). If that also fails, renders a static gradient matching the theme's gradient stops. |
| `waterLevel` outside 0.0–1.0 | Clamped to range. No crash. |

---

### 2.7 Services: TiltSensorManager

**Location**: `com.wane.app.animation.TiltSensorManager`
**Owner**: Backend Developer
**Consumers**: `SessionViewModel` (to pass `tiltState` to `WaterCanvas`)

| # | Signature | Description |
| - | --------- | ----------- |
| 1 | `val tiltFlow: Flow<TiltState>` | Continuous tilt data. Emits `Unavailable` once if no sensor, then closes. Emits `Available(x, y)` at ~50Hz, conflated and low-pass filtered (α=0.15). |
| 2 | `fun start()` | Register sensor listener. Called when session starts. |
| 3 | `fun stop()` | Unregister sensor listener. Called when session ends. Releases sensor resources. |

**Lifecycle:** `start()` on `SessionState.Running`, `stop()` on any transition out of `Running`. `SessionManager` is responsible for calling these.

**Error cases:**

| Condition | Behavior |
| --------- | -------- |
| No gyroscope sensor | `tiltFlow` emits `TiltState.Unavailable` and completes. |
| Sensor delayed/stuck | `conflate()` operator drops stale values. No backpressure buildup. |

---

### 2.8 UI → Services: IntentHelpers

**Location**: `com.wane.app.util.IntentHelpers`
**Owner**: Backend Developer (Utilities module)
**Consumer**: Frontend Developer (`BottomToolbar` composable in `SessionScreen`)

```kotlin
object IntentHelpers {
    fun openDialer(context: Context, number: String? = null)
    fun openContacts(context: Context)
    fun openSms(context: Context, number: String? = null)
}
```

| # | Function | Intent | Permission Required |
| - | -------- | ------ | ------------------- |
| 1 | `openDialer` | `ACTION_DIAL` with optional `tel:` URI | None |
| 2 | `openContacts` | `ACTION_VIEW` with `ContactsContract.Contacts.CONTENT_URI` | None |
| 3 | `openSms` | `ACTION_SENDTO` with `smsto:` URI | None |

**Error cases:**

| Condition | Behavior |
| --------- | -------- |
| No app resolves the intent (`ActivityNotFoundException`) | Shows `Toast`: "No app found". Does not crash. |
| `number` contains invalid characters | Sanitized via `Uri.encode()`. Does not crash. |

All intents include `FLAG_ACTIVITY_NEW_TASK` so they work from any context (including Service context).

---

### 2.9 Services: EmergencySafety

**Location**: `com.wane.app.util.EmergencySafety`
**Owner**: Backend Developer
**Consumers**: `WaneAccessibilityService`, `WaneNotificationListener`, `SessionManager`

```kotlin
object EmergencySafety {
    val EMERGENCY_NUMBERS: Set<String>          // immutable, hardcoded
    val NEVER_BLOCK_PACKAGES: Set<String>       // immutable, hardcoded

    fun isEmergencyNumber(number: String): Boolean
    fun isNeverBlockPackage(packageName: String): Boolean
}
```

**Contract guarantees (security-critical):**
1. `EMERGENCY_NUMBERS` and `NEVER_BLOCK_PACKAGES` are `val` (compile-time immutable). They are never loaded from preferences, database, or any mutable source.
2. `isEmergencyNumber()` strips non-digit characters before matching. Matches against suffixes (handles country prefixes).
3. Every blocking decision in `WaneAccessibilityService` and `WaneNotificationListener` MUST call `EmergencySafety` checks BEFORE evaluating session state.
4. The emergency exit flow has zero cooldown, zero rate limit, zero maximum attempts.

---

### 2.10 Services: WaneAccessibilityService

**Location**: `com.wane.app.service.WaneAccessibilityService`
**Owner**: Backend Developer
**Internal dependency**: `SessionManager.sessionState`, `EmergencySafety`

This service has no public API consumed by other modules. It is a system-registered `AccessibilityService` that reacts to `TYPE_WINDOW_STATE_CHANGED` events.

**Behavioral contract (important for UI developers to understand):**

| Condition | Behavior |
| --------- | -------- |
| Session is `Running` AND foreground app ∉ allowlist | Launches `MainActivity` with `FLAG_ACTIVITY_NEW_TASK \| FLAG_ACTIVITY_CLEAR_TOP` → user returns to water screen. |
| Session is `Running` AND foreground app ∈ allowlist | No action. Phone/Contacts/SMS/SystemUI/Settings are always allowed. |
| Session is NOT `Running` | No action on any event. |
| Service disabled by user or Android APM | `SessionManager` detects via `AccessibilityManager.isEnabled` check. Emits `SessionError.AccessibilityServiceDisabled`. Session continues (timer runs) but app blocking is degraded. |

**The allowlist is a superset of:** all packages in `IntentHelpers` targets + `NEVER_BLOCK_PACKAGES` + `com.wane.app` itself.

---

### 2.11 Services: WaneNotificationListener

**Location**: `com.wane.app.service.WaneNotificationListener`
**Owner**: Backend Developer
**Internal dependency**: `SessionManager.sessionState`, `EmergencySafety`, `PreferencesRepository.observeEmergencyContacts()`

No public API. System-registered `NotificationListenerService`.

**Behavioral contract:**

| Condition | Behavior |
| --------- | -------- |
| Session NOT `Running` | No filtering. All notifications pass through. |
| Session `Running`, notification from phone/contacts/SMS | Allow through (not snoozed). |
| Session `Running`, caller is emergency number | Allow through. |
| Session `Running`, caller is in emergency contacts list | Allow through. |
| Session `Running`, repeated caller (≥3 calls in 5 min) | Allow through + show "Repeated caller" system notification. |
| Session `Running`, all other notifications | `snoozeNotification(key, Long.MAX_VALUE)` — hidden but preserved. |
| Session ends (any reason) | All snoozed notifications are unsnoozed via `unsnoozeNotification(key)`. |

---

### 2.12 Services: WaterThemeCatalog

**Location**: `com.wane.app.animation.WaterThemeCatalog`
**Owner**: Backend Developer
**Consumers**: `SessionManager`, optionally `SettingsViewModel` for theme previews

```kotlin
object WaterThemeCatalog {
    fun getVisuals(themeId: String): WaterThemeVisuals?
    fun getAllVisuals(): List<WaterThemeVisuals>
    val defaultVisuals: WaterThemeVisuals
}
```

| # | Function | Description |
| - | -------- | ----------- |
| 1 | `getVisuals(themeId)` | Returns compile-time visual config for a theme. `null` if `themeId` unknown. |
| 2 | `getAllVisuals()` | All available theme visuals. |
| 3 | `defaultVisuals` | The "Still Water" theme visuals. Always non-null. |

**Note:** This catalog contains only visual parameters (shader uniforms). Purchase/ownership state is in `ThemeRepository` (Data layer). To show a theme in the picker, the UI combines `ThemeRepository.observeAllThemes()` (ownership) with `WaterThemeCatalog.getVisuals()` (preview rendering).

---

## 3. Event Contracts

Events are reactive streams observed by consumers. There are no callback registrations or event buses — all communication uses Kotlin `Flow` or `StateFlow`.

### 3.1 Session State Changes

| Producer | Type | Consumer(s) | Mechanism |
| -------- | ---- | ----------- | --------- |
| `SessionManager` | `StateFlow<SessionState>` | `SessionViewModel`, `HomeViewModel`, `WaneAccessibilityService`, `WaneNotificationListener` | `sessionManager.sessionState.collect { ... }` |

**Update frequency:** `Running` state emits every 50ms (timer tick). Other states emit once on transition.

**Thread safety:** `StateFlow` is thread-safe by default. Services read on their own coroutine scope. UI reads via `collectAsStateWithLifecycle()`.

---

### 3.2 Settings Changes

| Producer | Observable | Consumer(s) | Purpose |
| -------- | ---------- | ----------- | ------- |
| `PreferencesRepository` | `observeAutoLockConfig()` | `AutoLockScheduler` (Service), `AutoLockViewModel` (UI) | Service reacts to config changes in real time; UI shows current values. |
| `PreferencesRepository` | `observeSelectedThemeId()` | `SessionManager` (Service), `SettingsViewModel` (UI) | Service loads correct theme visuals; UI highlights active theme. |
| `PreferencesRepository` | `observeDefaultDuration()` | `HomeViewModel` (UI) | Home screen shows current default duration. |
| `PreferencesRepository` | `observeEmergencyContacts()` | `WaneNotificationListener` (Service), `SettingsViewModel` (UI) | Service updates breakthrough list; UI shows current contacts. |
| `PreferencesRepository` | `observeOnboardingCompleted()` | `MainActivity` / Nav setup (UI) | Determines start destination (Onboarding vs Home). |

**Guarantee:** All `observe*` methods emit the current value immediately on collection (DataStore's `data` flow behavior), then emit on every subsequent change.

---

### 3.3 Water Level Updates

| Producer | Data Path | Consumer |
| -------- | --------- | -------- |
| `SessionManager` timer | `SessionState.Running.waterLevel` | `SessionViewModel` → `WaterCanvas` composable |

**Data flow:**
```
SessionManager (50ms tick)
  → StateFlow<SessionState.Running> { waterLevel = remaining / total }
    → SessionViewModel.collectAsStateWithLifecycle()
      → WaterCanvas(waterLevel = state.waterLevel)
        → GL thread reads atomic float
          → Shader uniform u_waterLevel
```

No intermediate event bus. The value flows through existing `StateFlow` → Compose state → composable parameter → GL atomic.

---

### 3.4 Tilt Updates

| Producer | Data Path | Consumer |
| -------- | --------- | -------- |
| `TiltSensorManager` | `tiltFlow: Flow<TiltState>` | `SessionViewModel` → `WaterCanvas` composable |

**Data flow:**
```
SensorManager callback (50Hz)
  → callbackFlow + conflate + low-pass filter
    → SessionViewModel.collectAsState()
      → WaterCanvas(tiltState = tiltState)
        → GL thread reads atomic tiltX/tiltY
          → Shader uniforms u_tiltX, u_tiltY
```

---

### 3.5 Session Error Events

| Producer | Type | Consumer | Mechanism |
| -------- | ---- | -------- | --------- |
| `SessionManager` | `Flow<SessionError>` | `SessionViewModel`, `HomeViewModel` | Collected via `LaunchedEffect`. Each error consumed exactly once (`Channel`-backed). |

UI shows a non-blocking message (snackbar or inline) — never a blocking dialog for service errors.

---

## 4. Database Contracts

### 4.1 Table Ownership

| Table | Owning Module | Write Access | Read Access |
| ----- | ------------- | ------------ | ----------- |
| `focus_sessions` | Data (`SessionRepositoryImpl`) | Services via `SessionRepository.recordSession()` / `updateSessionEnd()` | UI via `SessionRepository.observe*()` |
| `water_themes` | Data (`ThemeRepositoryImpl`) | Services via `ThemeRepository.markThemePurchased()` | UI via `ThemeRepository.observe*()`, Services via `ThemeRepository.getThemeById()` |
| DataStore prefs | Data (`PreferencesRepositoryImpl`) | UI via `PreferencesRepository.set*()` | Services via `PreferencesRepository.observe*()`, UI via `PreferencesRepository.observe*()` |

### 4.2 Read Access Patterns

| Consumer | Data Source | Access Pattern | Frequency |
| -------- | ----------- | -------------- | --------- |
| `HomeViewModel` | `SessionRepository.observeCurrentStreak()` | `Flow<Int>`, collected on screen mount | Continuous while Home is visible |
| `HomeViewModel` | `PreferencesRepository.observeDefaultDuration()` | `Flow<Int>` | Continuous while Home is visible |
| `SessionViewModel` | `SessionManager.sessionState` | `StateFlow<SessionState>` | 50ms ticks while session active |
| `SettingsViewModel` | `PreferencesRepository.observe*()` | Multiple `Flow` combined | Continuous while Settings is visible |
| `SettingsViewModel` | `ThemeRepository.observeAllThemes()` | `Flow<List<WaterTheme>>` | Continuous while Settings is visible |
| `AutoLockScheduler` | `PreferencesRepository.observeAutoLockConfig()` | `Flow<AutoLockConfig>` | Continuous while app process is alive |
| `WaneNotificationListener` | `PreferencesRepository.observeEmergencyContacts()` | `Flow<List<String>>` | Continuous while service is bound |

### 4.3 Write Timing

| Writer | Table/Store | When |
| ------ | ----------- | ---- |
| `SessionManager` | `focus_sessions` INSERT | Session starts (endTime=0, status=COMPLETED as placeholder) |
| `SessionManager` | `focus_sessions` UPDATE | Session completes or user emergency-exits (sets endTime, actualDurationMs, status) |
| `ThemeRepository` | `water_themes` UPSERT | App first install (seed defaults) |
| `ThemeRepository` | `water_themes` UPDATE | IAP purchase confirmed |
| `PreferencesRepository` | DataStore | User changes any setting in UI |

---

## 5. ViewModel → Repository Wiring (Reference)

This section shows how each ViewModel connects to repositories and services. Each ViewModel is `@HiltViewModel` with constructor-injected dependencies.

### 5.1 OnboardingViewModel

```kotlin
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel()
```

| Reads | Writes |
| ----- | ------ |
| `preferencesRepository.observeOnboardingCompleted()` | `preferencesRepository.setOnboardingCompleted(true)` |
| — | `preferencesRepository.setDefaultDuration(minutes)` |

### 5.2 HomeViewModel

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val sessionRepository: SessionRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel()
```

| Reads | Writes / Actions |
| ----- | ---------------- |
| `preferencesRepository.observeDefaultDuration()` | `sessionManager.startSession(durationMs, themeId)` |
| `sessionRepository.observeCurrentStreak()` | `preferencesRepository.setDefaultDuration(minutes)` |
| `sessionManager.sessionState` (to know if session is active) | — |
| `sessionManager.errorEvents` | — |

### 5.3 SessionViewModel

```kotlin
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val tiltSensorManager: TiltSensorManager,
    private val waterThemeCatalog: WaterThemeCatalog,
    private val preferencesRepository: PreferencesRepository
) : ViewModel()
```

| Reads | Writes / Actions |
| ----- | ---------------- |
| `sessionManager.sessionState` | `sessionManager.requestEmergencyExit()` |
| `tiltSensorManager.tiltFlow` | `sessionManager.confirmSessionComplete()` |
| `preferencesRepository.observeSelectedThemeId()` → `waterThemeCatalog.getVisuals()` | — |
| `preferencesRepository.observeHapticFeedbackEnabled()` | — |

**UiState exposed to SessionScreen:**

```kotlin
data class SessionUiState(
    val waterLevel: Float = 1.0f,
    val tiltState: TiltState = TiltState.Unavailable,
    val themeVisuals: WaterThemeVisuals = WaterThemeCatalog.defaultVisuals,
    val elapsedMs: Long = 0L,
    val isExitSheetVisible: Boolean = false,
    val exitInput: String = "",
    val isSessionComplete: Boolean = false,
    val completedDurationMs: Long = 0L
)
```

### 5.4 SettingsViewModel

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val themeRepository: ThemeRepository,
    private val sessionRepository: SessionRepository
) : ViewModel()
```

| Reads | Writes |
| ----- | ------ |
| `preferencesRepository.observeDefaultDuration()` | `preferencesRepository.setDefaultDuration()` |
| `preferencesRepository.observeSelectedThemeId()` | `preferencesRepository.setSelectedThemeId()` |
| `preferencesRepository.observeAmbientSoundsEnabled()` | `preferencesRepository.setAmbientSoundsEnabled()` |
| `preferencesRepository.observeHapticFeedbackEnabled()` | `preferencesRepository.setHapticFeedbackEnabled()` |
| `preferencesRepository.observeEmergencyContacts()` | `preferencesRepository.setEmergencyContacts()` |
| `themeRepository.observeAllThemes()` | — |
| `sessionRepository.observeStreakInfo()` | `sessionRepository.clearAllSessions()` |

### 5.5 AutoLockViewModel

```kotlin
@HiltViewModel
class AutoLockViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel()
```

| Reads | Writes |
| ----- | ------ |
| `preferencesRepository.observeAutoLockConfig()` | `preferencesRepository.setAutoLockConfig(config)` |

---

## 6. Dependency Graph (compile-time module dependencies)

```
┌──────────────────────────────────────────────────────────┐
│                      shared/                             │
│   SessionState, FocusSession, WaterTheme,                │
│   WaterThemeVisuals, WaveParams, AutoLockConfig,         │
│   CompletionStatus, TiltState, WaneRoute, StreakInfo      │
└────────────────────┬─────────────────────────────────────┘
                     │ depends on (types only, no logic)
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
┌──────────┐  ┌────────────┐  ┌──────────────┐
│  ui/     │  │  service/  │  │    data/     │
│          │  │ animation/ │  │              │
│ Screens  │  │    util/   │  │ Repositories │
│ VMs      │  │            │  │ DAOs         │
│ Theme    │  │ Session    │  │ DataStore    │
│ Nav      │  │ Blocking   │  │ Database     │
│          │  │ Animation  │  │              │
└────┬─────┘  └─────┬──────┘  └──────────────┘
     │              │                  ▲
     │              │                  │
     │    consumes  │    consumes      │
     └──────────────┴──────────────────┘
         (via repository interfaces)
```

**Rules:**
- `shared/` has zero dependencies on other app packages.
- `data/` depends on `shared/` only.
- `service/` and `animation/` depend on `shared/` and `data/` (repository interfaces).
- `ui/` depends on `shared/`, `data/` (repository interfaces), and `service/` (`SessionManager`, `IntentHelpers`, `WaterThemeCatalog`).
- `ui/` depends on `animation/` only for the `WaterCanvas` composable.
- No circular dependencies.

---

## 7. Self-Verification

| # | Check | Status |
| - | ----- | ------ |
| 1 | Every cross-module communication has a defined contract | PASS — UI↔Services (§2.1, §2.8, §2.9), UI↔Data (§2.2–§2.4), Services↔Data (§2.5), Services↔Animation (§2.6–§2.7), system services (§2.10–§2.11) |
| 2 | All shared types fully specified with fields and types | PASS — 9 shared types (§1.1–§1.9) with Kotlin signatures, defaults, and invariants |
| 3 | Developer can implement any module from INTERFACES.md + their proposal | PASS — ViewModel wiring (§5), repository signatures (§2.2–§2.4), service APIs (§2.1), animation API (§2.6) are all implementation-ready |
| 4 | Error cases documented | PASS — every API contract includes error table |
| 5 | Types align with DB Engineer's entities | PASS — `FocusSession` matches entity fields, `CompletionStatus` matches enum, `AutoLockConfig` matches DataStore keys, `WaterTheme` matches entity |
| 6 | Types align with Backend Dev's state machine | PASS — `SessionState` matches FSM transitions, `TiltState` matches sensor architecture, `WaterThemeVisuals` matches shader uniform contract |
| 7 | Types align with Frontend Dev's UiState patterns | PASS — `SessionUiState` (§5.3) maps to Frontend's proposal, ViewModel dependencies match UDF pattern |
| 8 | No mutable shared state outside of defined contracts | PASS — all cross-module data flows through `StateFlow`, `Flow`, or `suspend` calls |
| 9 | Emergency safety constraints preserved | PASS — `EmergencySafety` contract (§2.9) specifies immutability, priority ordering, zero cooldown |
| 10 | Animation API is composable-friendly | PASS — `WaterCanvas` (§2.6) is a `@Composable` with standard parameters and `Modifier` support |
