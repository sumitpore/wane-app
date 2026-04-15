package com.wane.app.service

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import com.wane.app.service.di.AccessibilityServiceEntryPoint
import com.wane.app.util.EmergencySafety
import dagger.hilt.android.EntryPointAccessors

class WaneAccessibilityService : AccessibilityService() {
    private lateinit var appBlocker: AppBlocker

    private var imePackageCache: Set<String> = emptySet()
    private var imeCacheElapsedMs: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            val entryPoint =
                EntryPointAccessors.fromApplication(
                    applicationContext,
                    AccessibilityServiceEntryPoint::class.java,
                )
            appBlocker = entryPoint.appBlocker()
            refreshImeCache()
            Log.d(TAG, "Accessibility service connected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize accessibility service", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val packageName = event?.packageName?.toString() ?: return

            if (EmergencySafety.isNeverBlockPackage(packageName)) return
            if (isEnabledIme(packageName)) return

            if (!::appBlocker.isInitialized) return

            if (appBlocker.shouldBlockApp(packageName)) {
                appBlocker.redirectToWane()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onAccessibilityEvent error", e)
        }
    }

    private fun isEnabledIme(packageName: String): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - imeCacheElapsedMs > IME_CACHE_TTL_MS) {
            refreshImeCache()
        }
        return packageName in imePackageCache
    }

    private fun refreshImeCache() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imePackageCache = imm
                ?.enabledInputMethodList
                ?.mapNotNull { it.packageName }
                ?.toSet()
                ?: emptySet()
        } catch (_: Exception) {
            // Keep previous cache on failure
        }
        imeCacheElapsedMs = SystemClock.elapsedRealtime()
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
        private const val IME_CACHE_TTL_MS = 60_000L
    }
}
