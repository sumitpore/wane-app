package com.wane.app.animation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.wane.app.shared.TiltState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.conflate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Exposes low-pass filtered tilt as [TiltState]. Call [start] before collecting [tiltFlow] and
 * [stop] when done. Uses [SensorManager.SENSOR_DELAY_GAME] and α = 0.15 smoothing with
 * [conflate] to drop stale samples.
 */
@Singleton
class TiltSensorManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val gameRotation: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var prevX = 0f
    private var prevY = 0f
    private var filterInitialized = false

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            try {
                event ?: return
                when (event.sensor.type) {
                    Sensor.TYPE_GAME_ROTATION_VECTOR -> handleGameRotation(event)
                    Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event)
                }
            } catch (_: Throwable) {
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val _tiltFlow = MutableSharedFlow<TiltState>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val tiltFlow: Flow<TiltState> = _tiltFlow.asSharedFlow().conflate()

    private var listening = false

    fun start() {
        try {
            if (listening) return
            val sensor = gameRotation ?: accelerometer
            if (sensor == null) {
                _tiltFlow.tryEmit(TiltState.Unavailable)
                return
            }
            filterInitialized = false
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
            listening = true
        } catch (_: Throwable) {
        }
    }

    fun stop() {
        try {
            if (!listening) return
            sensorManager.unregisterListener(listener)
            listening = false
            filterInitialized = false
        } catch (_: Throwable) {
        }
    }

    private fun handleGameRotation(event: SensorEvent) {
        val r = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(r, event.values)
        val pitch = -asin(r[7].toDouble().coerceIn(-1.0, 1.0)).toFloat()
        val roll = atan2(r[6].toDouble(), r[8].toDouble()).toFloat()
        val nx = (roll / PI_F).coerceIn(-1f, 1f)
        val ny = (pitch / HALF_PI).coerceIn(-1f, 1f)
        emitFiltered(nx, ny)
    }

    private fun handleAccelerometer(event: SensorEvent) {
        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]
        val mag = sqrt(ax * ax + ay * ay + az * az).coerceAtLeast(0.01f)
        val nx = (ax / mag).coerceIn(-1f, 1f)
        val ny = (ay / mag).coerceIn(-1f, 1f)
        emitFiltered(nx, ny)
    }

    private fun emitFiltered(rawX: Float, rawY: Float) {
        if (!filterInitialized) {
            prevX = rawX
            prevY = rawY
            filterInitialized = true
        } else {
            prevX = applyLowPassFilter(rawX, prevX, ALPHA)
            prevY = applyLowPassFilter(rawY, prevY, ALPHA)
        }
        _tiltFlow.tryEmit(TiltState.Available(prevX, prevY))
    }

    private fun applyLowPassFilter(input: Float, previous: Float, alpha: Float = ALPHA): Float {
        return previous + alpha * (input - previous)
    }

    companion object {
        private const val ALPHA = 0.15f
        private const val PI_F = kotlin.math.PI.toFloat()
        private const val HALF_PI = (kotlin.math.PI / 2.0).toFloat()
    }
}
