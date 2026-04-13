# Tech Stack Proposal: Android Services & Water Animation Layer

**Author**: Backend Developer (Android Services Developer)
**Date**: 2026-04-13
**Status**: PROPOSED (awaiting HIL Gate 2)
**Scope**: `app/src/main/kotlin/com/wane/app/service/`, `app/src/main/kotlin/com/wane/app/animation/`, `app/src/main/kotlin/com/wane/app/util/`, `app/src/main/AndroidManifest.xml`

---

## 1. Water Animation Engine

### Domain/Service

Full-screen water rendering: 3 wave layers, gradient body, 5 caustic lights, gyroscope tilt response, touch ripples, and session-driven water-level drain. This is the hero visual of the entire product.

### Recommended: OpenGL ES 3.0 via `GLSurfaceView` + GLSL Fragment Shaders

- **OpenGL ES version**: 3.0 (GLSL ES 3.00)
- **Surface**: `GLSurfaceView` embedded in Compose via `AndroidView` composable
- **Render mode**: `RENDERMODE_CONTINUOUSLY` (60fps target)
- **Shader architecture**: Single full-screen quad with a fragment shader computing all water visuals (waves, gradient, caustics) in a single pass
- **Minimum API**: 18 (OpenGL ES 3.0), aligned with project min SDK target of API 26 (Android 8.0)

### Rationale

1. **Performance ceiling**: The water animation spec demands composite sine waves across 3 layers, a 4-stop vertical gradient, 5 radial-gradient caustic lights with oscillating radii, and real-time tilt/touch response -- all at 60fps on mid-range hardware. A GPU fragment shader performs all this math per-pixel per-frame in parallel, which is precisely what GPUs are designed for. Compose Canvas (`drawScope`) runs on the CPU via Skia software rasterization and cannot sustain 60fps for this workload on Snapdragon 600-series.

2. **Battery efficiency**: GPU shaders for a single full-screen quad draw call consume far less power than CPU-driven per-pixel math. A well-optimized single-pass GLSL shader with `mediump` precision stays comfortably under the 5% per hour battery budget. The GPU idles between frames when the shader completes in <4ms.

3. **Compose integration**: `GLSurfaceView` integrates cleanly into Compose via `AndroidView`. The GL surface renders on its own thread (the GL thread), so the Compose UI thread stays free for toolbar interactions, the End button, and status bar updates. Synchronization is achieved via atomic floats for `waterLevel`, `tiltX`, `tiltY`, and touch coordinates -- no locks needed.

4. **Device coverage**: OpenGL ES 3.0 is supported by 99%+ of devices running Android 8.0+ (our target). No fallback path needed unlike AGSL (API 33+).

5. **Theme extensibility**: Wave parameters (amplitude, frequency, speed, color), gradient stops, and caustic properties are all shader uniforms. Swapping a water theme is just updating a uniform buffer -- no shader recompilation needed.

### Alternatives Considered

| Alternative | Why Rejected |
|---|---|
| **AGSL / RuntimeShader** | Requires API 33 (Android 13). Would exclude ~25% of target devices on API 26-32. Also, AGSL runs within the Skia pipeline (View/Compose draw pass) which competes with UI element drawing for frame time. For a full-screen continuously-animated surface, a dedicated GL surface is architecturally cleaner. AGSL is ideal for UI-level effects (blurs, ripples on buttons) but not for a continuous 60fps full-screen animation engine. |
| **Jetpack Compose Canvas** | `Canvas { drawPath/drawRect }` uses CPU-side Skia rendering. At 2x resolution (as spec requires), drawing 3 wave layers with per-pixel sine computation + 5 radial gradients + the water gradient body would consume 12-16ms/frame on a Snapdragon 600, leaving zero headroom. No GPU shader support. Suitable for static or simple animated UI, not for a continuous fluid simulation. |
| **Vulkan** | Massive implementation complexity for a 2D full-screen quad. Vulkan's advantages (multi-threaded command buffers, fine-grained pipeline control) are irrelevant for a single-quad single-pass shader. Requires API 24+ and extensive boilerplate. Overkill. |
| **Custom `SurfaceView` + EGL** | Same GPU approach as GLSurfaceView but requires manual EGL context management (display, surface, config selection, swap chain). GLSurfaceView handles all of this automatically with a managed GL thread. No benefit, more code. |

### Modules Using This

- `animation/WaterRenderer.kt` -- `GLSurfaceView.Renderer` implementation
- `animation/WaterShaders.kt` -- GLSL vertex/fragment shader source strings
- `animation/WaterSurfaceView.kt` -- Custom `GLSurfaceView` subclass
- `animation/WaterCanvas.kt` -- `@Composable` wrapper using `AndroidView`
- `animation/WaterTheme.kt` -- Theme data class mapping to shader uniforms
- `ui/session/SessionScreen.kt` -- Consumes `WaterCanvas` composable (Frontend Dev ownership, but integrates our component)

### Implementation Architecture

```
┌─────────────────────────────────────────────────┐
│  Compose UI Thread                              │
│  ┌───────────────────────────────────────────┐  │
│  │ SessionScreen                             │  │
│  │  ├── WaterCanvas (AndroidView wrapper)    │  │
│  │  ├── StatusBar                            │  │
│  │  ├── Watermark / End button               │  │
│  │  └── BottomToolbar                        │  │
│  └───────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────┘
                       │ AndroidView hosts
┌──────────────────────▼──────────────────────────┐
│  GL Thread (managed by GLSurfaceView)           │
│  ┌───────────────────────────────────────────┐  │
│  │ WaterRenderer : GLSurfaceView.Renderer    │  │
│  │  onDrawFrame():                           │  │
│  │   1. Read atomic uniforms (waterLevel,    │  │
│  │      tiltX, tiltY, touchX, touchY, time)  │  │
│  │   2. glUseProgram(waterShader)            │  │
│  │   3. Set all uniforms                     │  │
│  │   4. Draw full-screen quad                │  │
│  │   5. Swap buffers (automatic)             │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

**Shader uniform contract** (values set per-frame from atomic state):

| Uniform | Type | Source |
|---|---|---|
| `u_time` | `float` | Monotonic clock, incremented +0.015/frame |
| `u_resolution` | `vec2` | Canvas width × height (2x device pixels) |
| `u_waterLevel` | `float` | 1.0 (full) → 0.0 (empty), driven by session timer |
| `u_tiltX`, `u_tiltY` | `float` | Gyroscope-derived tilt, low-pass filtered |
| `u_touchX`, `u_touchY` | `float` | Last touch position (normalized 0-1) |
| `u_touchTime` | `float` | Time since last touch (for ripple decay) |
| `u_wave1Color` .. `u_wave3Color` | `vec4` | Theme-driven wave colors |
| `u_gradientStops` | `vec4[4]` | Theme-driven gradient colors |
| `u_causticColor` | `vec4` | Theme-driven caustic center color |

---

## 2. AccessibilityService Architecture (App Blocking)

### Domain/Service

Foreground app detection and redirect: when a focus session is active, detect when any non-allowlisted app comes to the foreground and immediately redirect the user back to the Wane water screen.

### Recommended: `AccessibilityService` with `TYPE_WINDOW_STATE_CHANGED` + Strict Play Store Compliance Protocol

- **Service class**: `WaneAccessibilityService extends AccessibilityService`
- **Event filter**: `TYPE_WINDOW_STATE_CHANGED` only (narrowest scope)
- **Detection**: Extract package name from `AccessibilityEvent.packageName`
- **Response**: Launch `MainActivity` (water screen) via `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP` when blocked app detected

### Rationale

1. **Only viable approach**: Android provides no other API that can reliably detect foreground app changes in real-time and act on them. `UsageStatsManager` requires polling (100-500ms latency), which allows blocked apps to flash on screen before redirect -- unacceptable UX for a premium product. Every major focus app on the Play Store (AppBlock, Freedom, OFFTIME, Flipd, Stay Focused) uses AccessibilityService for this reason.

2. **Narrow scope minimizes policy risk**: Our service configuration requests only `TYPE_WINDOW_STATE_CHANGED` events. We do not request `TYPE_VIEW_CLICKED`, `TYPE_VIEW_TEXT_CHANGED`, or any content-reading flags. We read only the `packageName` field -- we never read window content, view hierarchy, or user input.

3. **Deterministic behavior**: Google Play policy explicitly permits "deterministic, rule-based automation where behavior follows a static, human-defined script." Our logic is exactly this: IF `packageName ∉ allowlist` AND `session.isActive` THEN `redirect()`. No AI, no autonomous decisions, no content analysis.

### Play Store Compliance Protocol

This is a high-risk permission. The following compliance measures are mandatory:

| Requirement | Implementation |
|---|---|
| **Prominent in-app disclosure** | Full-screen disclosure before requesting enable: "Wane needs accessibility access to bring the water screen forward during your session. This permission is only used to check which app is in the foreground. Wane does not read screen content, text, or passwords." |
| **Play Console declaration** | Non-accessibility tool declaration with detailed justification, video walkthrough showing the single-purpose use |
| **Accessibility service config XML** | `canRetrieveWindowContent="false"`, `canPerformGestures="false"`, `notificationTimeout="0"`, only `typeWindowStateChanged` in `accessibilityEventTypes` |
| **Privacy policy** | Explicit statement: "The Accessibility Service reads only the name of the foreground application. No screen content, passwords, or personal data is ever accessed." |
| **Data minimization** | Package name is checked in-memory only. Never logged, never stored, never transmitted. |
| **User can always disable** | Never prevent the user from turning off the service. Never use Device Admin to prevent uninstall. |
| **Android 17 APM handling** | Gracefully detect when APM revokes our service. Show a clear, non-alarmist message: "Water can't cover other apps right now. Your session timer continues." Fall back to notification-only mode. |

### Allowlist (hardcoded, non-configurable by user in v1)

```kotlin
val SESSION_ALLOWLIST = setOf(
    "com.android.dialer",
    "com.google.android.dialer",
    "com.samsung.android.dialer",
    "com.android.contacts",
    "com.google.android.contacts",
    "com.samsung.android.contacts",
    "com.android.mms",
    "com.google.android.apps.messaging",
    "com.samsung.android.messaging",
    "com.android.phone",
    "com.google.android.phone",
    "com.samsung.android.phone",
    "com.android.server.telecom",
    "com.android.incallui",
    "com.wane.app",
    // System UI surfaces that must never be blocked
    "com.android.systemui",
    "com.android.settings",
    "com.android.emergency",
    "com.google.android.apps.safetyhub",
)
```

The allowlist includes major OEM variants (Google, Samsung, AOSP) for Phone, Contacts, and SMS. Additional OEM packages (OnePlus, Xiaomi, Oppo) will be added during device testing.

### Alternatives Considered

| Alternative | Why Rejected |
|---|---|
| **UsageStatsManager polling** | 100-500ms detection latency. Blocked apps visibly flash on screen before redirect. Unacceptable for a premium UX where the water screen must feel impenetrable. Also consumes more battery due to continuous polling. |
| **Device Policy Manager / Device Admin** | Deprecated since Android 9. Requires enterprise MDM enrollment. Not suitable for consumer apps. Would prevent users from uninstalling the app, violating Play Store policy. |
| **Launcher replacement** | Would require users to switch their home launcher to Wane. Extremely high friction, low adoption. Doesn't cover the case where users switch apps via recent apps or notifications. |
| **UsageStatsManager + AccessibilityService hybrid** | Added complexity with no benefit. AccessibilityService alone provides real-time detection. UsageStatsManager adds nothing if we already have the accessibility event. |

### Modules Using This

- `service/WaneAccessibilityService.kt` -- Service implementation
- `service/AppBlocker.kt` -- Allowlist logic, redirect mechanism
- `util/PackageUtils.kt` -- OEM package name resolution helpers
- `AndroidManifest.xml` -- Service declaration, config XML reference

---

## 3. NotificationListenerService Architecture (Notification Filtering)

### Domain/Service

During active focus sessions, suppress all notifications except those from Phone, SMS, and Contacts apps. Allow emergency contact breakthrough (same number calling 3x in 5 minutes).

### Recommended: `NotificationListenerService` with Package-Based Filtering + `snoozeNotification()` Suppression

- **Service class**: `WaneNotificationListener extends NotificationListenerService`
- **Filtering**: `onNotificationPosted()` checks `sbn.packageName` against allowlist
- **Suppression method**: `snoozeNotification(key, Long.MAX_VALUE)` for blocked notifications during session; `unsnooze` all on session end
- **Emergency breakthrough**: Extract caller number from notification extras, track call frequency per-number in a time-windowed ring buffer

### Rationale

1. **Snooze instead of cancel**: `cancelNotification()` permanently removes the notification -- the user loses it forever. `snoozeNotification()` with a far-future time temporarily hides it, and we can `unsnooze` all snoozed notifications when the session ends. This preserves every notification the user received during their session.

2. **Package-based filtering is sufficient**: Phone, SMS, and Contacts notifications come from well-known system packages. The same allowlist used for AccessibilityService works here. No content inspection needed.

3. **Emergency breakthrough via extras**: Incoming call notifications from the Phone app include the caller number in `Notification.extras` (key `android.text` or `Notification.EXTRA_TEXT`). We can extract the number without reading any other notification content. The ring buffer approach (store last N call timestamps per number, check if count >= 3 within 300 seconds) is O(1) memory per tracked number and requires no persistence.

### Implementation Details

**Notification flow during active session:**

```
onNotificationPosted(sbn):
  1. IF session is NOT active → return (do nothing)
  2. IF sbn.packageName ∈ phoneAllowlist → allow through
  3. IF sbn is incoming call notification:
     a. Extract caller number
     b. IF caller is emergency contact → allow through
     c. IF isRepeatedCaller(number, 3, 5.minutes) → allow through + notify user
  4. ELSE → snoozeNotification(sbn.key, Long.MAX_VALUE)
  
onSessionEnd():
  1. For each snoozed notification key:
     unsnoozeNotification(key)
  2. Clear snoozed key list
  3. Clear repeated-caller ring buffer
```

**Repeated caller ring buffer:**

```kotlin
class RepeatedCallerTracker(
    private val threshold: Int = 3,
    private val windowMs: Long = 5 * 60 * 1000L
) {
    private val callLog = mutableMapOf<String, ArrayDeque<Long>>()

    fun recordCall(number: String): Boolean {
        val normalized = normalizeNumber(number)
        val timestamps = callLog.getOrPut(normalized) { ArrayDeque() }
        val now = SystemClock.elapsedRealtime()
        timestamps.addLast(now)
        // Evict expired entries
        while (timestamps.isNotEmpty() && now - timestamps.first() > windowMs) {
            timestamps.removeFirst()
        }
        return timestamps.size >= threshold
    }

    fun clear() = callLog.clear()
}
```

### Alternatives Considered

| Alternative | Why Rejected |
|---|---|
| **cancelNotification() for blocked notifications** | Permanently destroys the notification. User misses important messages they received during the session. Not acceptable. |
| **Do Not Disturb (DND) mode** | Cannot selectively allow Phone/SMS while blocking everything else. DND is all-or-nothing for categories. Also conflicts with user's own DND settings. |
| **Notification channels manipulation** | Apps control their own channels. We cannot disable another app's notification channel from our app without Device Admin. |

### Modules Using This

- `service/WaneNotificationListener.kt` -- Service implementation
- `service/RepeatedCallerTracker.kt` -- Emergency breakthrough logic
- `util/NotificationUtils.kt` -- Caller number extraction, notification category detection
- `AndroidManifest.xml` -- Service declaration

---

## 4. Session Management

### Domain/Service

Focus session lifecycle: start, pause (none -- sessions don't pause), tick (water level decrement), complete, emergency exit. The session must survive process death, screen off, and doze mode.

### Recommended: `ForegroundService` (type `specialUse`) + Kotlin Coroutines `Flow` Timer + Finite State Machine

- **Service class**: `WaneSessionService extends Service` (foreground)
- **Timer engine**: `kotlinx.coroutines.flow.flow { }` emitting ticks every 50ms within a `CoroutineScope` tied to the service lifecycle
- **State machine**: Sealed class with explicit transitions: `Idle → Running → Completing → Idle`, `Running → EmergencyExit → Idle`
- **Session state communication**: `StateFlow<SessionState>` exposed via a singleton `SessionManager` object, observed by both UI and other services

### Rationale

1. **ForegroundService is mandatory**: Android kills background services aggressively (especially on OEM-skinned devices like Samsung, Xiaomi, OnePlus). A ForegroundService with a persistent notification is the only way to guarantee the session timer survives screen-off and doze mode. The notification also serves as a session indicator and provides a quick-return tap target.

2. **Coroutines Flow over CountDownTimer**: `CountDownTimer` is a legacy API with millisecond precision issues, no cancellation support, and no testability. A coroutine-based `flow { delay(50); emit(tick) }` is cancellable, testable with `TestCoroutineScheduler`, and naturally integrates with `StateFlow` for UI observation. The 50ms tick interval matches the design spec's water level update frequency.

3. **Coroutines Flow over Handler/Runnable**: Handler-based timing requires manual lifecycle management, is not easily testable, and doesn't compose with Flow-based state observation. Coroutines are the idiomatic Kotlin approach.

4. **`specialUse` foreground service type**: Since Android 14 (API 34), foreground services must declare a type. Our session service doesn't fit standard categories (it's not media, location, or data sync). The `specialUse` type exists for cases like ours. Requires a Play Console declaration explaining the use case.

### State Machine

```
            startSession(durationMs)
  ┌──────┐ ─────────────────────────► ┌─────────┐
  │ Idle │                            │ Running │
  └──────┘ ◄─────────────────────────┘─────────┘
      ▲       sessionComplete()           │
      │                                   │ emergencyExit()
      │                                   ▼
      │    done()                   ┌──────────────┐
      ├─────────────────────────── │ Completing   │
      │                            └──────────────┘
      │    confirmed()             ┌──────────────┐
      └─────────────────────────── │ EmergencyExit│
                                   └──────────────┘
```

```kotlin
sealed class SessionState {
    data object Idle : SessionState()
    data class Running(
        val totalDurationMs: Long,
        val remainingMs: Long,
        val waterLevel: Float  // 1.0 → 0.0
    ) : SessionState()
    data class Completing(
        val totalDurationMs: Long
    ) : SessionState()
    data object EmergencyExit : SessionState()
}
```

### Timer Implementation

```kotlin
private fun createTimerFlow(totalMs: Long): Flow<Long> = flow {
    val startTime = SystemClock.elapsedRealtime()
    val endTime = startTime + totalMs
    while (true) {
        val now = SystemClock.elapsedRealtime()
        val remaining = (endTime - now).coerceAtLeast(0L)
        emit(remaining)
        if (remaining <= 0L) break
        delay(50) // 50ms tick = 20 updates/sec, matches design spec
    }
}
```

Using `SystemClock.elapsedRealtime()` instead of tracking accumulated delay ensures the timer stays accurate even if individual ticks are delayed by system load or doze mode wake-ups.

### Auto-Lock Integration

```kotlin
class ScreenLockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                if (AutoLockSettings.isEnabled && !isInSkipWindow() && !isCharging()) {
                    // Start grace period countdown
                    scheduleAutoLockSession(AutoLockSettings.gracePeriodMs)
                }
            }
            Intent.ACTION_USER_PRESENT -> {
                // User unlocked within grace period → cancel auto-lock
                cancelPendingAutoLock()
            }
        }
    }
}
```

### Alternatives Considered

| Alternative | Why Rejected |
|---|---|
| **CountDownTimer** | Legacy API. No structured cancellation. Accumulates drift over long sessions (30-120 min) because it adds delay to elapsed time rather than computing against a fixed endpoint. Not testable with coroutine test infrastructure. |
| **AlarmManager** | Designed for scheduling future events, not continuous ticking. Would require one alarm per 50ms tick, which Android would batch and defer under doze mode. Not suitable for a continuous timer. |
| **WorkManager** | Designed for deferrable, guaranteed background work. Not suitable for real-time, user-facing sessions that must tick continuously. Has a minimum 15-minute repeat interval for periodic work. |
| **Handler + Runnable** | Works but is the pre-coroutine approach. No structured concurrency, no cancellation scope, no built-in testing support, doesn't compose with StateFlow. |

### Modules Using This

- `service/WaneSessionService.kt` -- ForegroundService, timer coroutine, state machine
- `service/SessionManager.kt` -- Singleton exposing `StateFlow<SessionState>`, start/stop commands
- `service/ScreenLockReceiver.kt` -- BroadcastReceiver for `ACTION_SCREEN_OFF` / `ACTION_USER_PRESENT`
- `service/AutoLockScheduler.kt` -- Grace period logic, skip-window checking, skip-while-charging
- `AndroidManifest.xml` -- Service declaration with `foregroundServiceType="specialUse"`

---

## 5. Water Theme System

### Domain/Service

Swappable water themes that modify the visual character of the water animation. Default theme is free. Premium themes are purchasable via Google Play Billing.

### Recommended: Data-Driven Theme Model with Shader Uniform Mapping

- **Theme definition**: Kotlin data class containing all shader-modifiable parameters
- **Theme storage**: Bundled as sealed class variants (compile-time constants, no file I/O)
- **Theme switching**: Swap uniform values on the GL thread; no shader recompilation
- **Premium gating**: Google Play Billing Library v7 for purchases; theme unlock state stored in DataStore (DB Engineer's domain, but we define the interface)

### Rationale

1. **Shader uniforms are free to change**: OpenGL uniform updates are negligible-cost operations. Changing the entire visual character of the water (colors, wave shapes, caustic behavior) requires only updating ~20 float/vec4 uniforms. Zero frame drops during theme switches.

2. **Compile-time themes are simpler and more secure**: Themes bundled as Kotlin sealed class variants means no file parsing, no asset loading, no download logic. Reduces attack surface (no code injection via malicious theme files). Premium themes are already in the APK -- the purchase just flips an unlock flag.

3. **Theme parameters naturally map to the shader uniform contract**: Every visual property in the design spec (wave colors, amplitudes, frequencies, gradient stops, caustic colors) is already a shader uniform. The theme data class is a 1:1 mapping.

### Theme Data Model

```kotlin
data class WaterTheme(
    val id: String,
    val displayName: String,
    val isPremium: Boolean,
    val price: String?,  // e.g., "$2.99"
    // Wave layers (3)
    val wave1: WaveParams,
    val wave2: WaveParams,
    val wave3: WaveParams,
    // Water body gradient (4 stops)
    val gradientTop: Color,
    val gradientUpper: Color,
    val gradientLower: Color,
    val gradientBottom: Color,
    // Caustic lights
    val causticCenterColor: Color,
    val causticCount: Int,
    val causticBaseRadius: Float,
    val causticRadiusOscillation: Float,
    // Background gradient
    val backgroundStart: Color,
    val backgroundEnd: Color,
)

data class WaveParams(
    val color: Color,
    val amplitude: Float,
    val frequency: Float,
    val speed: Float,
)
```

### Theme Catalog (v1)

| Theme | Type | Description |
|---|---|---|
| **Still Water** (default) | Free | Blue tones from design spec. Calm, clear, default. |
| **Monsoon** | Premium ($2.99) | Darker grays/greens, higher wave amplitude, more turbulent. 4 extra caustic lights. |
| **Glacier** | Premium ($2.99) | Ice-white and pale cyan, low amplitude, slow drift, crystalline caustics. |
| **Koi** | Premium ($3.99) | Warm amber/gold gradient, medium waves, warm-white caustics. |
| **Bioluminescence** | Premium ($4.99) | Deep black body, electric cyan/green caustics with high brightness, slow waves. |

### Modules Using This

- `animation/WaterTheme.kt` -- Data model and sealed class theme catalog
- `animation/WaterRenderer.kt` -- Reads active theme, maps to shader uniforms
- `service/SessionManager.kt` -- Provides active theme to renderer on session start
- Frontend Dev dependency: Theme picker UI reads theme catalog and displays previews

---

## 6. Gyroscope / Sensor Integration

### Domain/Service

Tilt-responsive water: the water surface subtly shifts in response to device orientation, creating a "water in a glass" parallax effect.

### Recommended: `SensorManager` with `TYPE_GAME_ROTATION_VECTOR` + Complementary Low-Pass Filter + Coroutine Flow Bridge

- **Sensor type**: `TYPE_GAME_ROTATION_VECTOR` (fuses accelerometer + gyroscope without magnetometer; avoids magnetic interference)
- **Sampling rate**: `SENSOR_DELAY_GAME` (~20ms / 50Hz) -- sufficient for fluid visual response without excessive battery drain
- **Filtering**: Complementary low-pass filter with α = 0.15 (smooth, laggy feel that matches water physics)
- **Data flow**: Sensor callback → `callbackFlow` → `conflate()` → low-pass filter → atomic tilt values read by GL thread

### Rationale

1. **`TYPE_GAME_ROTATION_VECTOR` over raw gyroscope**: Raw gyroscope requires manual integration (summing angular velocity × dt) which drifts over time. The game rotation vector sensor performs hardware-level sensor fusion (accelerometer + gyroscope) and outputs stable orientation quaternions. The "game" variant excludes the magnetometer, avoiding jumps from magnetic interference (phone cases, metal desks).

2. **`SENSOR_DELAY_GAME` over `SENSOR_DELAY_FASTEST`**: 50Hz is more than sufficient for a visual tilt effect that's smoothed through a low-pass filter anyway. `SENSOR_DELAY_FASTEST` (200Hz+) would waste battery for no perceptible visual improvement. The low-pass filter with α=0.15 means the effective visual response time is ~130ms -- perfectly "watery."

3. **Coroutine callbackFlow bridge**: Wrapping the `SensorEventListener` in a `callbackFlow` makes it lifecycle-aware, composable with other flows, and cancellable when the session ends. `conflate()` ensures we process only the latest sensor value, dropping stale readings if the GL thread reads slower than the sensor reports.

4. **Graceful degradation**: Not all devices have gyroscopes (budget phones may only have accelerometers). If `TYPE_GAME_ROTATION_VECTOR` is unavailable, fall back to `TYPE_ACCELEROMETER` alone with gravity-based tilt estimation. If neither is available, tilt response is simply disabled -- the water still animates beautifully without it.

### Implementation Architecture

```kotlin
class TiltSensorManager(context: Context) {
    private val sensorManager = context.getSystemService<SensorManager>()
    
    val tiltFlow: Flow<TiltState> = callbackFlow {
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        if (sensor == null) {
            trySend(TiltState.Unavailable)
            awaitClose()
            return@callbackFlow
        }
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(TiltState.Available(
                    tiltX = event.values[0],
                    tiltY = event.values[1]
                ))
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
    .conflate()
    .map { raw -> applyLowPassFilter(raw) }

    private var filteredX = 0f
    private var filteredY = 0f
    private val alpha = 0.15f

    private fun applyLowPassFilter(state: TiltState): TiltState {
        if (state !is TiltState.Available) return state
        filteredX = alpha * state.tiltX + (1 - alpha) * filteredX
        filteredY = alpha * state.tiltY + (1 - alpha) * filteredY
        return TiltState.Available(filteredX, filteredY)
    }
}

sealed class TiltState {
    data object Unavailable : TiltState()
    data class Available(val tiltX: Float, val tiltY: Float) : TiltState()
}
```

### Alternatives Considered

| Alternative | Why Rejected |
|---|---|
| **Raw `TYPE_GYROSCOPE`** | Requires manual time-integration to get orientation from angular velocity. Drifts over long sessions (30-120 min). Needs complementary filter with accelerometer anyway -- which is exactly what `GAME_ROTATION_VECTOR` does in hardware. |
| **`TYPE_ROTATION_VECTOR`** | Includes magnetometer, which causes jumps when near magnetic sources (phone cases, tablet stands, car mounts). For a visual effect, magnetic north alignment is irrelevant. |
| **`SENSOR_DELAY_FASTEST`** | 200+ Hz creates unnecessary battery drain and CPU overhead from callback processing. The visual result is identical to 50Hz after our α=0.15 low-pass filter. |
| **`SensorManager` in the GL thread directly** | Sensor callbacks arrive on the main (or handler) thread by default. Running them on the GL thread would require a custom Handler and couples sensor lifecycle to GL lifecycle. The callbackFlow + atomic value approach cleanly decouples them. |

### Modules Using This

- `animation/TiltSensorManager.kt` -- Sensor registration, filtering, Flow emission
- `animation/WaterRenderer.kt` -- Reads tilt values as shader uniforms
- `service/WaneSessionService.kt` -- Starts/stops tilt sensor with session lifecycle

---

## 7. Intent System (Basic Phone Mode)

### Domain/Service

During active sessions, the bottom toolbar provides 3 buttons: Phone, Contacts, SMS. Tapping each launches the corresponding native Android app via implicit intents.

### Recommended: Implicit Intents with OEM-Aware Fallback Resolution

- **Phone**: `Intent(Intent.ACTION_DIAL)` -- opens dialer without requiring `CALL_PHONE` permission
- **Contacts**: `Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)` -- opens native contacts app
- **SMS**: `Intent(Intent.ACTION_VIEW).apply { type = "vnd.android-dir/mms-sms" }` with fallback to `Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))`

### Rationale

1. **ACTION_DIAL not ACTION_CALL**: `ACTION_DIAL` opens the dialer UI pre-populated but doesn't initiate the call. No `CALL_PHONE` runtime permission needed. The user controls the call action. This is both safer and requires no permission.

2. **Implicit over explicit intents**: Different OEMs have different default dialer/contacts/SMS apps. Implicit intents let Android resolve to the user's default. We don't need to maintain a mapping of OEM package names for launching -- only for the AccessibilityService allowlist where we need to recognize them.

3. **Fallback chain**: Intent resolution can fail if no app handles the intent (rare but possible on heavily customized devices). Each intent helper includes a `try/catch` with a fallback to a broader intent action, and ultimately a user-facing toast if nothing resolves.

### Implementation

```kotlin
object IntentHelpers {
    fun openDialer(context: Context, number: String? = null) {
        val intent = if (number != null) {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}"))
        } else {
            Intent(Intent.ACTION_DIAL)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.safeStartActivity(intent)
    }

    fun openContacts(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = ContactsContract.Contacts.CONTENT_URI
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.safeStartActivity(intent)
    }

    fun openSms(context: Context, number: String? = null) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${number ?: ""}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.safeStartActivity(intent)
    }

    private fun Context.safeStartActivity(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No app found", Toast.LENGTH_SHORT).show()
        }
    }
}
```

### Modules Using This

- `util/IntentHelpers.kt` -- All intent construction and launching
- Frontend Dev dependency: `BottomToolbar` composable calls `IntentHelpers` methods on button tap

---

## 8. Emergency Safety System

### Domain/Service

Emergency safety is the highest-priority system in the entire app. It must be architecturally impossible for a focus session to prevent emergency communication. This is non-negotiable and overrides all other system behaviors.

### Recommended: Multi-Layer Defense with Hardcoded Bypass + Zero-Persistence Safety Checks

The emergency system is not a feature -- it's a constraint that every other system must respect. It operates at three levels:

### Layer 1: Never-Blocked Emergency Numbers

Emergency numbers are hardcoded at the constant level and checked before any blocking decision.

```kotlin
object EmergencySafety {
    // Hardcoded. Never loaded from preferences. Never configurable.
    val EMERGENCY_NUMBERS = setOf(
        "911", "112", "999", "000", "110", "119", "108",
        "911#", "*911", "#911"  // Variants
    )
    
    // System surfaces that must never be intercepted
    val NEVER_BLOCK_PACKAGES = setOf(
        "com.android.emergency",
        "com.google.android.apps.safetyhub",   // Personal Safety
        "com.android.systemui",
        "com.android.settings",
        "com.android.server.telecom",
        "com.android.incallui",
    )
    
    fun isEmergencyNumber(number: String): Boolean {
        val cleaned = number.replace("[^0-9#*]".toRegex(), "")
        return EMERGENCY_NUMBERS.any { cleaned.endsWith(it) || cleaned == it }
    }
}
```

### Layer 2: Repeated Caller Breakthrough

Same number calling 3 times within 5 minutes triggers breakthrough. Implemented in `RepeatedCallerTracker` (see Section 3).

The NotificationListenerService checks every incoming call notification:

```
IF callerNumber is emergency number → ALWAYS allow
IF callerNumber is in user's emergency contacts → ALWAYS allow
IF isRepeatedCaller(callerNumber, threshold=3, window=5min) → allow + show "Repeated caller" notification
```

### Layer 3: Emergency Exit Flow

Long-press the "End" button (3 seconds) to trigger the emergency exit sheet. User must type "EXIT" to confirm. This is deliberate friction that prevents accidental exits while ensuring anyone who truly needs to exit can do so.

```kotlin
sealed class ExitFlow {
    data object Hidden : ExitFlow()
    data object LongPressDetected : ExitFlow()  // Show sheet
    data class TypingConfirmation(val input: String) : ExitFlow()
    data object Confirmed : ExitFlow()  // End session
}
```

**Time constraint**: The exit confirmation must never have a cooldown, rate limit, or lockout. If someone is in an emergency, they must be able to exit immediately by typing "EXIT."

### Layer 4: System Emergency Features Never Interfered With

| Android Feature | How We Ensure Non-Interference |
|---|---|
| Emergency SOS (power button x5) | System-level, launches before any app can intercept. Our AccessibilityService allowlists `com.android.emergency`. |
| Fall/Crash detection | Runs at system level. We allowlist Safety Hub packages. |
| Medical ID on lock screen | Rendered by SystemUI, which is in our NEVER_BLOCK_PACKAGES. |
| Emergency calling from lock screen | Handled by `com.android.server.telecom`, always allowlisted. |
| Battery Saver / Low Power | We stop the water animation shader when battery < 15% and switch to a static gradient. Session timer continues. |

### Verification Checklist

- [ ] `EmergencySafety.EMERGENCY_NUMBERS` is a `val` (immutable), not loaded from any mutable source
- [ ] `NEVER_BLOCK_PACKAGES` is a `val`, not filtered or reduced by any other code path
- [ ] `WaneAccessibilityService.onAccessibilityEvent()` checks `NEVER_BLOCK_PACKAGES` before any session-active check
- [ ] `WaneNotificationListener.onNotificationPosted()` checks emergency numbers before any session-active check
- [ ] The emergency exit flow has no cooldown, rate limit, or maximum attempts
- [ ] No code path can disable the repeated-caller-breakthrough feature (it's always on, not a setting)
- [ ] Battery critical mode degrades visuals but never stops session timer or disables emergency exit

### Modules Using This

- `util/EmergencySafety.kt` -- Constants, emergency number detection
- `service/WaneAccessibilityService.kt` -- Consults `NEVER_BLOCK_PACKAGES` before every redirect
- `service/WaneNotificationListener.kt` -- Consults emergency checks before every snooze
- `service/RepeatedCallerTracker.kt` -- Repeated caller detection
- `service/WaneSessionService.kt` -- Emergency exit state machine transitions

---

## 9. Dependency Summary

| Library | Version | Purpose | Module |
|---|---|---|---|
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.9.x | Coroutine dispatchers, Flow, StateFlow for timer and sensor | service/, animation/ |
| `androidx.core:core-ktx` | 1.15.x | Kotlin extensions for Android framework APIs | util/, service/ |
| `androidx.lifecycle:lifecycle-service` | 2.8.x | LifecycleService base class for ForegroundService | service/ |
| `com.android.billingclient:billing-ktx` | 7.x | Google Play Billing for premium theme purchases | service/ (purchase verification), data/ (unlock state) |
| Android SDK OpenGL ES 3.0 | Platform API | GPU-accelerated water rendering | animation/ |
| Android SDK SensorManager | Platform API | Gyroscope/rotation sensor access | animation/ |
| Android SDK AccessibilityService | Platform API | Foreground app detection and redirect | service/ |
| Android SDK NotificationListenerService | Platform API | Notification interception and filtering | service/ |

No third-party rendering libraries (no libGDX, no Filament, no Processing). The water animation is a single GLSL shader -- adding a game engine or 3D library for one full-screen quad would bloat the APK far beyond the 30MB target.

---

## 10. AndroidManifest.xml Requirements

```xml
<!-- Permissions -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- OpenGL ES 3.0 requirement (soft, not hard) -->
<uses-feature android:glEsVersion="0x00030000" android:required="false" />

<!-- Session Foreground Service -->
<service
    android:name=".service.WaneSessionService"
    android:foregroundServiceType="specialUse"
    android:exported="false">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="focus_session_timer" />
</service>

<!-- Accessibility Service (App Blocking) -->
<service
    android:name=".service.WaneAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
    android:exported="false">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>

<!-- Notification Listener Service -->
<service
    android:name=".service.WaneNotificationListener"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    android:exported="false">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>

<!-- Screen Lock Receiver (Auto-Lock) -->
<receiver
    android:name=".service.ScreenLockReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.SCREEN_OFF" />
        <action android:name="android.intent.action.USER_PRESENT" />
    </intent-filter>
</receiver>
```

**Accessibility service config** (`res/xml/accessibility_service_config.xml`):

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100"
    android:canRetrieveWindowContent="false"
    android:canPerformGestures="false"
    android:description="@string/accessibility_service_description"
    android:settingsActivity="com.wane.app.ui.settings.SettingsActivity" />
```

---

## 11. Performance Budget

| Component | CPU Budget (per frame) | GPU Budget (per frame) | Battery Impact |
|---|---|---|---|
| Water shader (GL thread) | ~0.5ms (uniform setup) | ~3-4ms (fragment shader, mid-range GPU) | ~2.5%/hr |
| Sensor processing | ~0.1ms (filter, 50Hz) | 0 | ~0.3%/hr |
| Session timer | ~0.01ms (50ms ticks) | 0 | ~0.1%/hr |
| AccessibilityService | ~0.05ms (event processing) | 0 | ~0.2%/hr |
| NotificationListenerService | ~0.05ms (per notification) | 0 | ~0.1%/hr |
| Foreground notification | 0 | 0 | ~0.3%/hr |
| **Total estimated** | **~0.7ms** | **~3-4ms** | **~3.5%/hr** |

This leaves comfortable headroom within the 16.6ms frame budget (60fps) and the 5%/hr battery target.

**Low-battery mode**: When battery drops below 15%, reduce the water animation to 30fps (`RENDERMODE_WHEN_DIRTY` with manual `requestRender()` every 33ms) and disable caustic lights (set caustic count uniform to 0). This halves the GPU power consumption while maintaining a visually acceptable water surface.

---

## 12. Self-Verification

| # | Question | Answer |
|---|---|---|
| 1 | Does the proposal cover all 8 areas? | Yes: Water Animation (§1), AccessibilityService (§2), NotificationListenerService (§3), Session Management (§4), Water Themes (§5), Gyroscope/Sensors (§6), Intent System (§7), Emergency Safety (§8) |
| 2 | Does the water animation approach meet 60fps/battery requirements? | Yes: Single-pass GLSL fragment shader on dedicated GL thread. Estimated 3-4ms GPU per frame on Snapdragon 600 (16.6ms budget). Estimated 3.5%/hr total battery (5% target). Low-battery mode drops to 30fps at <15%. |
| 3 | Is the AccessibilityService approach compliant with Google Play policy? | Yes with caveats: Narrowest possible scope (TYPE_WINDOW_STATE_CHANGED only), canRetrieveWindowContent=false, prominent in-app disclosure, Play Console declaration, privacy policy disclosure. Policy risk exists but is mitigated by following the same approach as approved apps (AppBlock, Freedom, Stay Focused). Android 17 APM may disable the service -- graceful fallback to notification-only mode. |
| 4 | Are emergency safety features never-compromised? | Yes: Hardcoded immutable constants for emergency numbers and never-block packages. Emergency checks execute before any blocking logic. No cooldowns on emergency exit. Repeated caller breakthrough is always-on (not a setting). System emergency features (SOS, fall detection, medical ID) are architecturally unreachable by our services. |
| 5 | Is the proposal specific enough for implementation? | Yes: Each section includes concrete class names, file paths, code samples, data models, and architectural diagrams. The shader uniform contract, state machine, intent helpers, and emergency safety checks are all implementation-ready. |
