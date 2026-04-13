# Backend Developer Conventions — Wane

**Created**: 2026-04-13
**Tech Stack**: Kotlin 2.3.20, OpenGL ES 3.0, Coroutines, Hilt 2.57.1, ForegroundService (`specialUse`)
**Scope**: `app/src/main/kotlin/com/wane/app/service/`, `app/src/main/kotlin/com/wane/app/animation/`, `app/src/main/kotlin/com/wane/app/util/`, `app/src/main/AndroidManifest.xml`

> Prerequisites: Read `CONVENTIONS.md` (general) and `ARCHITECTURE.md` (system overview, file ownership) before writing service or animation code.

---

## 1. File Organization

```
service/
├── WaneSessionService.kt         -- ForegroundService, timer coroutine, state machine
├── SessionManager.kt             -- Singleton, StateFlow<SessionState>, start/stop API
├── WaneAccessibilityService.kt   -- App blocking service
├── AppBlocker.kt                 -- Allowlist logic, redirect mechanism
├── WaneNotificationListener.kt   -- Notification filtering service
├── RepeatedCallerTracker.kt      -- Emergency breakthrough ring buffer
├── ScreenLockReceiver.kt         -- BroadcastReceiver for auto-lock
└── AutoLockScheduler.kt          -- Grace period, skip-window, skip-while-charging

animation/
├── WaterRenderer.kt              -- GLSurfaceView.Renderer implementation
├── WaterShaders.kt               -- GLSL vertex/fragment shader source strings
├── WaterSurfaceView.kt           -- Custom GLSurfaceView subclass
├── WaterCanvas.kt                -- @Composable wrapper (AndroidView)
├── WaterTheme.kt                 -- Theme data class → shader uniform mapping
└── TiltSensorManager.kt          -- Gyroscope sensor, callbackFlow, low-pass filter

util/
├── EmergencySafety.kt            -- Hardcoded emergency constants
├── IntentHelpers.kt              -- Dialer, contacts, SMS intent launchers
├── PackageUtils.kt               -- OEM package name resolution
└── NotificationUtils.kt          -- Caller number extraction helpers
```

---

## 2. Service Naming

| Kind | Pattern | Example |
| ---- | ------- | ------- |
| Android Service class | `Wane{Purpose}Service` | `WaneSessionService`, `WaneAccessibilityService` |
| Service helper / logic | `{Purpose}{Noun}` | `AppBlocker`, `RepeatedCallerTracker`, `AutoLockScheduler` |
| Manager singleton | `{Domain}Manager` | `SessionManager` |
| Utility object | `{Domain}{Utils/Helpers/Safety}` | `IntentHelpers`, `EmergencySafety`, `PackageUtils` |
| BroadcastReceiver | `{Purpose}Receiver` | `ScreenLockReceiver` |

---

## 3. Session State Machine

All session state transitions go through `SessionManager`. No service, UI, or test code modifies session state directly.

```kotlin
sealed class SessionState {
    data object Idle : SessionState()
    data class Running(
        val totalDurationMs: Long,
        val remainingMs: Long,
        val waterLevel: Float,   // 1.0 → 0.0
    ) : SessionState()
    data class Completing(val totalDurationMs: Long) : SessionState()
    data object EmergencyExit : SessionState()
}
```

**Rules:**
- `SessionManager` exposes `StateFlow<SessionState>` — the single source of truth for session lifecycle.
- Valid transitions: `Idle → Running`, `Running → Completing`, `Running → EmergencyExit`, `Completing → Idle`, `EmergencyExit → Idle`.
- Any other transition is a programming error. Add a `check()` or `error()` in transition methods.
- Timer ticks update `Running.remainingMs` and `Running.waterLevel` atomically via `_state.update { }`.
- The timer uses `SystemClock.elapsedRealtime()` as the time source — never `System.currentTimeMillis()` (wall clock drifts with NTP).

---

## 4. Emergency Safety — Code Review Gate

**This is the highest-priority convention in the entire project.**

Emergency safety checks ALWAYS execute before any blocking or filtering logic. This ordering is not optional and must be verified in every code review.

```kotlin
// CORRECT order in WaneAccessibilityService.onAccessibilityEvent()
override fun onAccessibilityEvent(event: AccessibilityEvent) {
    val pkg = event.packageName?.toString() ?: return
    if (pkg in EmergencySafety.NEVER_BLOCK_PACKAGES) return  // FIRST
    if (!SessionManager.isActive) return                      // SECOND
    if (pkg in SESSION_ALLOWLIST) return                       // THIRD
    redirectToWater()                                          // LAST
}

// CORRECT order in WaneNotificationListener.onNotificationPosted()
override fun onNotificationPosted(sbn: StatusBarNotification) {
    if (isEmergencyNumber(sbn)) return                         // FIRST
    if (isEmergencyContact(sbn)) return                        // SECOND
    if (isRepeatedCaller(sbn)) { notifyBreakthrough(); return } // THIRD
    if (!SessionManager.isActive) return                       // FOURTH
    if (sbn.packageName in PHONE_ALLOWLIST) return             // FIFTH
    snoozeNotification(sbn.key, Long.MAX_VALUE)                // LAST
}
```

**Rules:**
- `EmergencySafety.EMERGENCY_NUMBERS` and `EmergencySafety.NEVER_BLOCK_PACKAGES` are `val` (immutable `Set<String>`). They are never loaded from preferences, database, or any mutable source.
- The emergency exit flow (long-press End → type "EXIT") has no cooldown, no rate limit, no maximum attempts.
- The repeated-caller-breakthrough feature is always on. It is not a user setting and cannot be disabled.
- Battery critical mode degrades visuals (30fps, no caustics) but never stops the timer, disables emergency exit, or disables repeated-caller breakthrough.

---

## 5. GLSL Shader Conventions

| Convention | Rule |
| ---------- | ---- |
| Precision | Use `mediump` for all floating-point variables. Only use `highp` when precision artifacts are visible (document why). |
| Uniform naming | `u_camelCase` prefix — e.g., `u_time`, `u_waterLevel`, `u_tiltX`, `u_wave1Color` |
| Varying naming | `v_camelCase` prefix — e.g., `v_texCoord` |
| Attribute naming | `a_camelCase` prefix — e.g., `a_position` |
| Shader source location | Raw string constants in `WaterShaders.kt`. Not loaded from `assets/` at runtime. |
| GLSL version | `#version 300 es` — matches OpenGL ES 3.0 target |

### Shader Uniform Contract

Every shader uniform exposed to the theme system or sensor system must be documented in `WaterShaders.kt`:

```kotlin
/**
 * u_time       : float   — Monotonic clock, +0.015/frame
 * u_resolution : vec2    — Canvas width × height in device pixels
 * u_waterLevel : float   — 1.0 (full) → 0.0 (empty), driven by session timer
 * u_tiltX      : float   — Low-pass filtered gyroscope X
 * u_tiltY      : float   — Low-pass filtered gyroscope Y
 */
```

---

## 6. GL Thread / Main Thread Synchronization

The water renderer runs on the GL thread (managed by `GLSurfaceView`). The session timer and sensor data arrive on the main thread or coroutine dispatchers.

**Rules:**
- Use `AtomicReference<Float>` (or Kotlin `atomic` from `kotlinx.atomicfu`) for values shared between the GL thread and main thread: `waterLevel`, `tiltX`, `tiltY`, `touchX`, `touchY`, `touchTime`.
- Never use `synchronized` blocks or `Mutex` for per-frame data — the GL thread must never block waiting for the main thread.
- The GL thread reads atomics in `onDrawFrame()`. The main thread writes atomics from coroutine scopes. This is a single-writer / single-reader pattern; no locks needed.
- Shader uniform updates (`glUniform*`) happen only on the GL thread inside `onDrawFrame()`.

---

## 7. Coroutine Scope Usage

| Context | Scope | Dispatcher |
| ------- | ----- | ---------- |
| ForegroundService (`WaneSessionService`) | `lifecycleScope` (from `LifecycleService`) | `Dispatchers.Default` for timer ticks |
| AccessibilityService | `CoroutineScope(SupervisorJob() + Dispatchers.Main)`, cancelled in `onDestroy()` | `Dispatchers.Main` |
| NotificationListenerService | `CoroutineScope(SupervisorJob() + Dispatchers.Main)`, cancelled in `onListenerDisconnected()` | `Dispatchers.Main` |
| SessionManager (singleton) | Application-scoped `CoroutineScope` injected by Hilt | `Dispatchers.Default` |

**Rules:**
- Always use structured concurrency. Every `CoroutineScope` must be cancelled when the owning component is destroyed.
- Never use `GlobalScope`.
- Timer flow: `flow { while(true) { emit(tick); delay(50) } }` — collected in `lifecycleScope` of the foreground service.
- Sensor flow: `callbackFlow { }` with `conflate()` to drop stale sensor readings.

---

## 8. Sensor Data Flow

```
SensorManager.registerListener()
    → callbackFlow { trySend(raw) }
        → conflate()
            → map { applyLowPassFilter(it) }
                → collect { atomicTiltX.set(it.x); atomicTiltY.set(it.y) }
```

**Rules:**
- Use `TYPE_GAME_ROTATION_VECTOR` as primary sensor. Fall back to `TYPE_ACCELEROMETER` if unavailable. Disable tilt if neither exists.
- Sampling rate: `SENSOR_DELAY_GAME` (~50Hz). Never use `SENSOR_DELAY_FASTEST`.
- Low-pass filter: complementary filter with α = 0.15 (defined as a constant, not inlined).
- Register sensors in `WaneSessionService.onCreate()`, unregister in `onDestroy()`.
- The `TiltSensorManager` class owns all sensor lifecycle — services call `start()` / `stop()`, not raw `SensorManager` APIs.

---

## 9. Intent Helpers

All implicit intent construction lives in `util/IntentHelpers.kt` as an `object` with static methods.

**Rules:**
- Every intent launch is wrapped in `try/catch(ActivityNotFoundException)` with a user-facing fallback (Toast).
- Use `ACTION_DIAL` (not `ACTION_CALL`) for the phone — no `CALL_PHONE` permission needed.
- Use `ACTION_SENDTO` with `smsto:` URI for SMS.
- Use `ACTION_VIEW` with `ContactsContract.Contacts.CONTENT_URI` for contacts.
- Always add `FLAG_ACTIVITY_NEW_TASK` when launching from a service context.

```kotlin
private fun Context.safeStartActivity(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No app found", Toast.LENGTH_SHORT).show()
    }
}
```

---

## 10. AccessibilityService Constraints

**Rules:**
- The service config XML (`res/xml/accessibility_service_config.xml`) must set:
  - `accessibilityEventTypes="typeWindowStateChanged"` — nothing else
  - `canRetrieveWindowContent="false"`
  - `canPerformGestures="false"`
- The service reads only `event.packageName`. Never read window content, view hierarchy, or user input text.
- Package names are checked in-memory only. Never logged to disk, never stored in a database, never transmitted.
- On Android 17+ (APM), gracefully detect service revocation. Show a non-alarmist message: "Water can't cover other apps right now. Your session timer continues." Fall back to notification-only mode.

---

## 11. Manifest Entries

**Rules:**
- All services declared in `AndroidManifest.xml` must have `android:exported="false"` unless they require a system binding permission (AccessibilityService and NotificationListenerService have `exported="false"` + their respective `BIND_*` permissions).
- The foreground service declares `android:foregroundServiceType="specialUse"` with the `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` property set to `"focus_session_timer"`.
- OpenGL ES 3.0 is declared with `android:required="false"` — the app must handle devices without GPU support gracefully.
- Never add permissions beyond what is declared in the approved ARCHITECTURE.md manifest section. New permissions require team discussion.

---

## 12. Water Theme System

**Rules:**
- Theme parameters are defined as a Kotlin `data class WaterTheme` — all wave colors, amplitudes, frequencies, gradient stops, and caustic properties.
- Themes are compile-time constants (sealed class variants or `object` instances). No runtime theme file loading.
- Theme switching updates shader uniforms only — no shader recompilation, no `GLSurfaceView` recreation.
- The default theme ("Still Water") is always available and cannot be locked behind a purchase.

---

## 13. Error Handling in Services

**Rules:**
- Services must never crash. Wrap all callback methods (`onAccessibilityEvent`, `onNotificationPosted`, `onStartCommand`) in top-level `try/catch` blocks that log errors but do not propagate exceptions.
- Use `Log.e(TAG, "message", throwable)` for error logging in services. Define `TAG` as a `companion object` constant: `private const val TAG = "WaneSessionService"`.
- If the GL surface fails to initialize (no OpenGL ES 3.0 support), fall back to a static gradient background rendered via Compose Canvas. The session timer still runs.
