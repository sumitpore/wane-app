package com.wane.app.service

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.wane.app.MainActivity
import com.wane.app.shared.SessionState
import com.wane.app.util.EmergencySafety
import com.wane.app.util.PackageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBlocker
    @Inject
    constructor(
        private val sessionManager: SessionManager,
        @ApplicationContext private val context: Context,
    ) {
        @Volatile private var cachedAllowlist: Set<String> = emptySet()

        @Volatile private var cacheElapsedMs: Long = 0L

        private val fullScreenExemptions = ConcurrentHashMap<String, FullScreenExemption>()

        private fun getSessionAllowlist(): Set<String> {
            val now = SystemClock.elapsedRealtime()
            if (cachedAllowlist.isNotEmpty() && (now - cacheElapsedMs) < ALLOWLIST_TTL_MS) {
                return cachedAllowlist
            }
            val fresh =
                buildSet {
                    addAll(EmergencySafety.NEVER_BLOCK_PACKAGES)
                    addAll(PackageUtils.resolveDialerPackages(context))
                    addAll(PackageUtils.resolveContactsPackages(context))
                    addAll(PackageUtils.resolveSmsPackages(context))
                    addAll(PackageUtils.resolveImePackages(context))
                    add("com.wane.app")
                }
            cachedAllowlist = fresh
            cacheElapsedMs = now
            return fresh
        }

        fun shouldBlockApp(packageName: String): Boolean {
            if (EmergencySafety.isNeverBlockPackage(packageName)) return false
            if (sessionManager.sessionState.value !is SessionState.Running) return false
            if (packageName in getSessionAllowlist()) return false
            if (isFullScreenExempt(packageName)) return false
            return true
        }

        fun addFullScreenExemption(notificationKey: String, packageName: String) {
            val expiration = SystemClock.elapsedRealtime() + FULL_SCREEN_EXEMPTION_TTL_MS
            fullScreenExemptions.putIfAbsent(notificationKey, FullScreenExemption(packageName, expiration))
        }

        fun removeFullScreenExemption(notificationKey: String) {
            fullScreenExemptions.remove(notificationKey)
        }

        fun clearFullScreenExemptions() {
            fullScreenExemptions.clear()
        }

        private fun isFullScreenExempt(packageName: String): Boolean {
            val now = SystemClock.elapsedRealtime()
            val iterator = fullScreenExemptions.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.expirationElapsedMs < now) {
                    iterator.remove()
                } else if (entry.value.packageName == packageName) {
                    return true
                }
            }
            return false
        }

        fun redirectToWane() {
            val intent =
                Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            context.startActivity(intent)
        }

        private data class FullScreenExemption(
            val packageName: String,
            val expirationElapsedMs: Long,
        )

        companion object {
            private const val ALLOWLIST_TTL_MS = 30_000L
            private const val FULL_SCREEN_EXEMPTION_TTL_MS = 60_000L
        }
    }
