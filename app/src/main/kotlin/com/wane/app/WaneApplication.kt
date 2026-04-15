package com.wane.app

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import com.wane.app.service.ScreenLockReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WaneApplication : Application() {
    private val screenLockReceiver = ScreenLockReceiver()

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenLockReceiver, filter)
    }
}
