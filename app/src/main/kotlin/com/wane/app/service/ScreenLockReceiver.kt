package com.wane.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.Intent.ACTION_SCREEN_ON
import android.content.Intent.ACTION_USER_PRESENT
import android.util.Log
import com.wane.app.service.di.ScreenLockServiceEntryPoint
import dagger.hilt.android.EntryPointAccessors

class ScreenLockReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        if (context == null || intent == null) return
        try {
            val app = context.applicationContext
            val entryPoint =
                EntryPointAccessors.fromApplication(
                    app,
                    ScreenLockServiceEntryPoint::class.java,
                )
            val scheduler = entryPoint.autoLockScheduler()
            when (intent.action) {
                ACTION_USER_PRESENT -> scheduler.onScreenUnlocked()
                ACTION_SCREEN_OFF -> scheduler.onScreenLocked()
                ACTION_SCREEN_ON -> Unit
                else -> Unit
            }
        } catch (e: Exception) {
            Log.e(TAG, "onReceive failed", e)
        }
    }

    companion object {
        private const val TAG = "ScreenLockReceiver"
    }
}
