package com.wane.app.shared

sealed interface WaneRoute {
    data object Onboarding : WaneRoute

    data object Home : WaneRoute

    data object Session : WaneRoute

    data object Settings : WaneRoute

    data object AutoLockSettings : WaneRoute
}
