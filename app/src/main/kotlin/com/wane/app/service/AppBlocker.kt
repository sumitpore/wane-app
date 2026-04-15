package com.wane.app.service

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.wane.app.MainActivity
import com.wane.app.shared.SessionState
import com.wane.app.util.EmergencySafety
import com.wane.app.util.PackageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBlocker @Inject constructor(
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context,
) {

    @Volatile private var cachedAllowlist: Set<String> = emptySet()
    @Volatile private var cacheElapsedMs: Long = 0L

    private fun getSessionAllowlist(): Set<String> {
        val now = SystemClock.elapsedRealtime()
        if (cachedAllowlist.isNotEmpty() && (now - cacheElapsedMs) < ALLOWLIST_TTL_MS) {
            return cachedAllowlist
        }
        val fresh = buildSet {
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
        return true
    }

    fun redirectToWane() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)
    }

    companion object {
        private const val ALLOWLIST_TTL_MS = 30_000L
    }
}
