package com.wane.app.shared

sealed class TiltState {
    data object Unavailable : TiltState()
    data class Available(
        val tiltX: Float,
        val tiltY: Float,
    ) : TiltState()
}
