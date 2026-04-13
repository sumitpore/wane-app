package com.wane.app.animation

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.wane.app.shared.TiltState
import com.wane.app.shared.WaterThemeVisuals
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max

/**
 * Renders the procedural water effect on the GL thread. Main thread updates atomics; no
 * synchronized blocks on per-frame data.
 */
class WaterRenderer : GLSurfaceView.Renderer {

    private val waterLevel = AtomicReference(0.5f)
    private val tiltX = AtomicReference(0f)
    private val tiltY = AtomicReference(0f)
    private val touchX = AtomicReference(0.5f)
    private val touchY = AtomicReference(0.5f)
    private val touchTime = AtomicReference(-1f)
    private val touchPending = AtomicBoolean(false)

    private val themeRef = AtomicReference(WaterThemeCatalog.defaultVisuals)
    private val batteryPercent = AtomicInteger(100)

    val shaderFailed = AtomicBoolean(false)

    private var program = 0
    private var glEsVersion = 2
    private var quadBuffer: FloatBuffer? = null

    private var uResolution = -1
    private var uTime = -1
    private var uWaterLevel = -1
    private var uTiltX = -1
    private var uTiltY = -1
    private var uTouchX = -1
    private var uTouchY = -1
    private var uTouchTime = -1
    private var uBackgroundStart = -1
    private var uBackgroundEnd = -1
    private var uGradientTop = -1
    private var uGradientUpper = -1
    private var uGradientLower = -1
    private var uGradientBottom = -1
    private var uWave1Color = -1
    private var uWave1Amplitude = -1
    private var uWave1Frequency = -1
    private var uWave1Speed = -1
    private var uWave2Color = -1
    private var uWave2Amplitude = -1
    private var uWave2Frequency = -1
    private var uWave2Speed = -1
    private var uWave3Color = -1
    private var uWave3Amplitude = -1
    private var uWave3Frequency = -1
    private var uWave3Speed = -1
    private var uCausticCenterColor = -1
    private var uCausticCount = -1
    private var uCausticBaseRadius = -1
    private var uCausticRadiusOscillation = -1

    private var timeSeconds = 0f
    private var viewportW = 1
    private var viewportH = 1

    private val fallbackClearRgb = floatArrayOf(0.02f, 0.06f, 0.12f)

    fun setWaterLevel(value: Float) {
        waterLevel.set(value.coerceIn(0f, 1f))
    }

    fun setTiltState(state: TiltState) {
        when (state) {
            TiltState.Unavailable -> {
                tiltX.set(0f)
                tiltY.set(0f)
            }
            is TiltState.Available -> {
                tiltX.set(state.tiltX)
                tiltY.set(state.tiltY)
            }
        }
    }

    fun setThemeVisuals(visuals: WaterThemeVisuals) {
        themeRef.set(visuals)
    }

    /**
     * Records a new touch in UV space; the GL thread assigns [touchTime] to the current [timeSeconds]
     * on the next frame so it stays aligned with [u_time].
     */
    fun notifyTouch(uvX: Float, uvY: Float) {
        try {
            touchX.set(uvX.coerceIn(0f, 1f))
            touchY.set(uvY.coerceIn(0f, 1f))
            touchPending.set(true)
        } catch (_: Throwable) {
            // Never crash services / UI thread callers
        }
    }

    fun clearTouch() {
        try {
            touchTime.set(-1f)
            touchPending.set(false)
        } catch (_: Throwable) {
        }
    }

    fun setBatteryPercent(percent: Int) {
        batteryPercent.set(percent.coerceIn(0, 100))
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        try {
            onSurfaceCreatedInner()
        } catch (t: Throwable) {
            Log.e(TAG, "onSurfaceCreated failed", t)
            shaderFailed.set(true)
        }
    }

    private fun onSurfaceCreatedInner() {
        shaderFailed.set(false)
        val versionStr = GLES20.glGetString(GLES20.GL_VERSION) ?: ""
        val major = parseGlMajorVersion(versionStr)
        glEsVersion = if (major >= 3) 3 else 2

        val vert = if (glEsVersion >= 3) WaterShaders.VERTEX_SHADER else WaterShaders.VERTEX_SHADER_ES2
        val frag = if (glEsVersion >= 3) WaterShaders.FRAGMENT_SHADER else WaterShaders.FRAGMENT_SHADER_ES2

        val vs = compileShader(GLES20.GL_VERTEX_SHADER, vert)
        val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, frag)
        if (vs == 0 || fs == 0) {
            Log.e(TAG, "Shader compilation failed (GL ES $glEsVersion)")
            shaderFailed.set(true)
            GLES20.glReleaseShaderCompiler()
            return
        }

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vs)
        GLES20.glAttachShader(program, fs)
        if (glEsVersion < 3) {
            GLES20.glBindAttribLocation(program, 0, "a_position")
        }
        GLES20.glLinkProgram(program)
        val link = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, link, 0)
        GLES20.glDeleteShader(vs)
        GLES20.glDeleteShader(fs)
        if (link[0] == 0) {
            Log.e(TAG, "Program link failed: ${GLES20.glGetProgramInfoLog(program)}")
            GLES20.glDeleteProgram(program)
            program = 0
            shaderFailed.set(true)
            return
        }

        cacheUniformLocations()

        val quad = floatArrayOf(
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
        )
        val bb = ByteBuffer.allocateDirect(quad.size * 4).order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(quad)
        fb.position(0)
        quadBuffer = fb

        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
    }

    private fun parseGlMajorVersion(versionStr: String): Int {
        if (versionStr.isEmpty()) return 2
        val match = Regex("""(\d+)\.""").find(versionStr) ?: return 2
        return match.groupValues[1].toIntOrNull() ?: 2
    }

    private fun cacheUniformLocations() {
        uResolution = GLES20.glGetUniformLocation(program, "u_resolution")
        uTime = GLES20.glGetUniformLocation(program, "u_time")
        uWaterLevel = GLES20.glGetUniformLocation(program, "u_waterLevel")
        uTiltX = GLES20.glGetUniformLocation(program, "u_tiltX")
        uTiltY = GLES20.glGetUniformLocation(program, "u_tiltY")
        uTouchX = GLES20.glGetUniformLocation(program, "u_touchX")
        uTouchY = GLES20.glGetUniformLocation(program, "u_touchY")
        uTouchTime = GLES20.glGetUniformLocation(program, "u_touchTime")
        uBackgroundStart = GLES20.glGetUniformLocation(program, "u_backgroundStart")
        uBackgroundEnd = GLES20.glGetUniformLocation(program, "u_backgroundEnd")
        uGradientTop = GLES20.glGetUniformLocation(program, "u_gradientTop")
        uGradientUpper = GLES20.glGetUniformLocation(program, "u_gradientUpper")
        uGradientLower = GLES20.glGetUniformLocation(program, "u_gradientLower")
        uGradientBottom = GLES20.glGetUniformLocation(program, "u_gradientBottom")
        uWave1Color = GLES20.glGetUniformLocation(program, "u_wave1Color")
        uWave1Amplitude = GLES20.glGetUniformLocation(program, "u_wave1Amplitude")
        uWave1Frequency = GLES20.glGetUniformLocation(program, "u_wave1Frequency")
        uWave1Speed = GLES20.glGetUniformLocation(program, "u_wave1Speed")
        uWave2Color = GLES20.glGetUniformLocation(program, "u_wave2Color")
        uWave2Amplitude = GLES20.glGetUniformLocation(program, "u_wave2Amplitude")
        uWave2Frequency = GLES20.glGetUniformLocation(program, "u_wave2Frequency")
        uWave2Speed = GLES20.glGetUniformLocation(program, "u_wave2Speed")
        uWave3Color = GLES20.glGetUniformLocation(program, "u_wave3Color")
        uWave3Amplitude = GLES20.glGetUniformLocation(program, "u_wave3Amplitude")
        uWave3Frequency = GLES20.glGetUniformLocation(program, "u_wave3Frequency")
        uWave3Speed = GLES20.glGetUniformLocation(program, "u_wave3Speed")
        uCausticCenterColor = GLES20.glGetUniformLocation(program, "u_causticCenterColor")
        uCausticCount = GLES20.glGetUniformLocation(program, "u_causticCount")
        uCausticBaseRadius = GLES20.glGetUniformLocation(program, "u_causticBaseRadius")
        uCausticRadiusOscillation = GLES20.glGetUniformLocation(program, "u_causticRadiusOscillation")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        try {
            viewportW = max(1, width)
            viewportH = max(1, height)
            GLES20.glViewport(0, 0, viewportW, viewportH)
        } catch (t: Throwable) {
            Log.e(TAG, "onSurfaceChanged failed", t)
            shaderFailed.set(true)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        try {
            onDrawFrameInner()
        } catch (t: Throwable) {
            Log.e(TAG, "onDrawFrame failed", t)
            shaderFailed.set(true)
        }
    }

    private fun onDrawFrameInner() {
        if (shaderFailed.get()) {
            GLES20.glClearColor(fallbackClearRgb[0], fallbackClearRgb[1], fallbackClearRgb[2], 1f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            return
        }

        timeSeconds += FRAME_DT
        if (touchPending.getAndSet(false)) {
            touchTime.set(timeSeconds)
        }

        val wl = waterLevel.get()
        val tx = tiltX.get()
        val ty = tiltY.get()
        val tcx = touchX.get()
        val tcy = touchY.get()
        val tt = touchTime.get()
        val theme = themeRef.get()
        val battery = batteryPercent.get()
        val causticCount = if (battery < 15) 0 else theme.causticCount

        GLES20.glUseProgram(program)

        GLES20.glUniform2f(uResolution, viewportW.toFloat(), viewportH.toFloat())
        GLES20.glUniform1f(uTime, timeSeconds)
        GLES20.glUniform1f(uWaterLevel, wl)
        GLES20.glUniform1f(uTiltX, tx)
        GLES20.glUniform1f(uTiltY, ty)
        GLES20.glUniform1f(uTouchX, tcx)
        GLES20.glUniform1f(uTouchY, tcy)
        GLES20.glUniform1f(uTouchTime, tt)

        val bgS = theme.backgroundStart.toRgbaFloats()
        val bgE = theme.backgroundEnd.toRgbaFloats()
        GLES20.glUniform4fv(uBackgroundStart, 1, bgS, 0)
        GLES20.glUniform4fv(uBackgroundEnd, 1, bgE, 0)

        GLES20.glUniform4fv(uGradientTop, 1, theme.gradientTop.toRgbaFloats(), 0)
        GLES20.glUniform4fv(uGradientUpper, 1, theme.gradientUpper.toRgbaFloats(), 0)
        GLES20.glUniform4fv(uGradientLower, 1, theme.gradientLower.toRgbaFloats(), 0)
        GLES20.glUniform4fv(uGradientBottom, 1, theme.gradientBottom.toRgbaFloats(), 0)

        GLES20.glUniform4fv(uWave1Color, 1, theme.wave1.color.toRgbaFloats(), 0)
        GLES20.glUniform1f(uWave1Amplitude, theme.wave1.amplitude)
        GLES20.glUniform1f(uWave1Frequency, theme.wave1.frequency)
        GLES20.glUniform1f(uWave1Speed, theme.wave1.speed)

        GLES20.glUniform4fv(uWave2Color, 1, theme.wave2.color.toRgbaFloats(), 0)
        GLES20.glUniform1f(uWave2Amplitude, theme.wave2.amplitude)
        GLES20.glUniform1f(uWave2Frequency, theme.wave2.frequency)
        GLES20.glUniform1f(uWave2Speed, theme.wave2.speed)

        GLES20.glUniform4fv(uWave3Color, 1, theme.wave3.color.toRgbaFloats(), 0)
        GLES20.glUniform1f(uWave3Amplitude, theme.wave3.amplitude)
        GLES20.glUniform1f(uWave3Frequency, theme.wave3.frequency)
        GLES20.glUniform1f(uWave3Speed, theme.wave3.speed)

        GLES20.glUniform4fv(uCausticCenterColor, 1, theme.causticCenterColor.toRgbaFloats(), 0)
        GLES20.glUniform1i(uCausticCount, causticCount)
        GLES20.glUniform1f(uCausticBaseRadius, theme.causticBaseRadius)
        GLES20.glUniform1f(uCausticRadiusOscillation, theme.causticRadiusOscillation)

        val buffer = quadBuffer
        if (buffer != null) {
            buffer.position(0)
            GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 0, buffer)
            GLES20.glEnableVertexAttribArray(0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            GLES20.glDisableVertexAttribArray(0)
        }

        GLES20.glFlush()
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Shader compile error: ${GLES20.glGetShaderInfoLog(shader)}")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    private companion object {
        private const val TAG = "WaterRenderer"
        private const val FRAME_DT = 0.015f
    }
}

private fun Long.toRgbaFloats(): FloatArray {
    val a = ((this ushr 24) and 0xFF) / 255f
    val r = ((this ushr 16) and 0xFF) / 255f
    val g = ((this ushr 8) and 0xFF) / 255f
    val b = (this and 0xFF) / 255f
    return floatArrayOf(r, g, b, a)
}
