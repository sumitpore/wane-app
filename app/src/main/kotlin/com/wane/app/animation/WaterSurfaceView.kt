package com.wane.app.animation

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.opengl.GLSurfaceView
import android.os.BatteryManager
import android.util.AttributeSet
import android.view.Choreographer
import android.view.MotionEvent
import com.wane.app.shared.TiltState
import com.wane.app.shared.WaterThemeVisuals
import kotlin.math.max

/**
 * Hosts [WaterRenderer] with ES 3.0 when available, otherwise ES 2.0, continuous rendering,
 * normalized touch, and battery level forwarded for caustic LOD.
 */
class WaterSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    val renderer: WaterRenderer = WaterRenderer()

    var onShaderFailedChanged: ((Boolean) -> Unit)? = null
    var onTouchNormalized: ((Float, Float) -> Unit)? = null

    private var lastShaderFailed: Boolean = false
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            try {
                val failed = renderer.shaderFailed.get()
                if (failed != lastShaderFailed) {
                    lastShaderFailed = failed
                    onShaderFailedChanged?.invoke(failed)
                }
            } catch (_: Throwable) {
            }
            choreographer?.postFrameCallback(this)
        }
    }
    private var choreographer: Choreographer? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            try {
                if (intent?.action != Intent.ACTION_BATTERY_CHANGED) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    val pct = (level * 100) / scale
                    renderer.setBatteryPercent(pct)
                }
            } catch (_: Throwable) {
            }
        }
    }

    init {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val cfg = am.deviceConfigurationInfo
            val supportsEs3 = cfg.reqGlEsVersion >= 0x00030000
            setEGLContextClientVersion(if (supportsEs3) 3 else 2)
        } catch (_: Throwable) {
            setEGLContextClientVersion(2)
        }
        preserveEGLContextOnPause = true
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        try {
            choreographer = Choreographer.getInstance()
            choreographer?.postFrameCallback(frameCallback)
            val appCtx = context.applicationContext
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appCtx.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                appCtx.registerReceiver(batteryReceiver, filter)
            }
        } catch (_: Throwable) {
        }
    }

    override fun onDetachedFromWindow() {
        try {
            choreographer?.removeFrameCallback(frameCallback)
            choreographer = null
            context.applicationContext.unregisterReceiver(batteryReceiver)
        } catch (_: Throwable) {
        }
        super.onDetachedFromWindow()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                val w = max(1, width)
                val h = max(1, height)
                val nx = (event.x / w.toFloat()).coerceIn(0f, 1f)
                val ny = (event.y / h.toFloat()).coerceIn(0f, 1f)
                renderer.notifyTouch(nx, ny)
                onTouchNormalized?.invoke(nx, ny)
            }
        } catch (_: Throwable) {
        }
        return true
    }

    fun updateWaterLevel(level: Float) {
        try {
            renderer.setWaterLevel(level)
        } catch (_: Throwable) {
        }
    }

    fun updateTiltState(state: TiltState) {
        try {
            renderer.setTiltState(state)
        } catch (_: Throwable) {
        }
    }

    fun updateThemeVisuals(visuals: WaterThemeVisuals) {
        try {
            renderer.setThemeVisuals(visuals)
        } catch (_: Throwable) {
        }
    }

    fun isShaderFailed(): Boolean = renderer.shaderFailed.get()
}
