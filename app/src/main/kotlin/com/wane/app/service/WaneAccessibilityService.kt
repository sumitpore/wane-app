package com.wane.app.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.wane.app.service.di.AccessibilityServiceEntryPoint
import com.wane.app.util.EmergencySafety
import dagger.hilt.android.EntryPointAccessors

class WaneAccessibilityService : AccessibilityService() {

    private lateinit var appBlocker: AppBlocker

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                AccessibilityServiceEntryPoint::class.java,
            )
            appBlocker = entryPoint.appBlocker()
            Log.d(TAG, "Accessibility service connected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize accessibility service", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val packageName = event?.packageName?.toString() ?: return

            if (EmergencySafety.isNeverBlockPackage(packageName)) return

            if (!::appBlocker.isInitialized) return

            if (appBlocker.shouldBlockApp(packageName)) {
                appBlocker.redirectToWane()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onAccessibilityEvent error", e)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility service destroyed")
    }

    companion object {
        private const val TAG = "WaneA11yService"
    }
}
