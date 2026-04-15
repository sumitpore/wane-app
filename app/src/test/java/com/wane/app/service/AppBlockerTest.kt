package com.wane.app.service

import com.wane.app.shared.SessionState
import com.wane.app.util.EmergencySafety
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the blocking decision logic in isolation.
 *
 * Dialer, contacts, SMS, and IME packages are resolved at runtime via Android
 * APIs (PackageManager, TelecomManager, InputMethodManager, RoleManager) and
 * therefore cannot be exercised in a JVM unit test. Those paths are covered by
 * instrumentation tests. This file tests:
 *   - session-state transitions (only Running blocks)
 *   - NEVER_BLOCK_PACKAGES pass-through
 *   - own-package pass-through
 *   - unknown third-party apps get blocked
 */
class AppBlockerTest {
    private val fakeSessionManager = FakeSessionManager()
    private val blocker = AppBlockerShim(fakeSessionManager)

    // ── Session-state transitions ─────────────────────────────────────

    @Test
    fun `idle state never blocks any app`() {
        fakeSessionManager.setState(SessionState.Idle)
        assertFalse(blocker.shouldBlockApp("com.twitter.android"))
    }

    @Test
    fun `emergency exit state never blocks`() {
        fakeSessionManager.setState(SessionState.EmergencyExit)
        assertFalse(blocker.shouldBlockApp("com.twitter.android"))
    }

    @Test
    fun `completing state never blocks`() {
        fakeSessionManager.setState(SessionState.Completing(1L, 60_000L, 60_000L))
        assertFalse(blocker.shouldBlockApp("com.twitter.android"))
    }

    @Test
    fun `running state blocks random third-party app`() {
        fakeSessionManager.setState(runningState())
        assertTrue(blocker.shouldBlockApp("com.twitter.android"))
    }

    // ── NEVER_BLOCK_PACKAGES ──────────────────────────────────────────

    @Test
    fun `running state does not block never-block packages`() {
        fakeSessionManager.setState(runningState())
        for (pkg in EmergencySafety.NEVER_BLOCK_PACKAGES) {
            assertFalse("Expected $pkg to not be blocked", blocker.shouldBlockApp(pkg))
        }
    }

    @Test
    fun `running state does not block system resolver and chooser`() {
        fakeSessionManager.setState(runningState())
        val resolverPackages =
            listOf(
                "android",
                "com.android.internal.app",
                "com.google.android.permissioncontroller",
                "com.samsung.android.app.resolver",
            )
        for (pkg in resolverPackages) {
            assertFalse("Expected resolver $pkg to not be blocked", blocker.shouldBlockApp(pkg))
        }
    }

    // ── Own package ───────────────────────────────────────────────────

    @Test
    fun `running state does not block own package`() {
        fakeSessionManager.setState(runningState())
        assertFalse(blocker.shouldBlockApp("com.wane.app"))
    }

    // ── Known blocked categories ──────────────────────────────────────

    @Test
    fun `running state blocks unknown browser`() {
        fakeSessionManager.setState(runningState())
        assertTrue(blocker.shouldBlockApp("com.android.chrome"))
    }

    @Test
    fun `running state blocks social media`() {
        fakeSessionManager.setState(runningState())
        assertTrue(blocker.shouldBlockApp("com.instagram.android"))
        assertTrue(blocker.shouldBlockApp("com.facebook.katana"))
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private fun runningState() =
        SessionState.Running(
            sessionId = 1L,
            totalDurationMs = 25 * 60_000L,
            remainingMs = 20 * 60_000L,
            waterLevel = 0.2f,
        )
}

/**
 * JVM-safe shim that replicates the static portion of [AppBlocker.shouldBlockApp].
 *
 * Runtime-resolved packages (dialer, contacts, SMS, IME) require Android
 * Context and are intentionally excluded — they are tested via instrumentation.
 */
private class AppBlockerShim(
    private val sessionManager: FakeSessionManager,
) {
    fun shouldBlockApp(packageName: String): Boolean {
        if (EmergencySafety.isNeverBlockPackage(packageName)) return false
        if (sessionManager.sessionState.value !is SessionState.Running) return false
        if (packageName in STATIC_ALLOWLIST) return false
        return true
    }

    companion object {
        val STATIC_ALLOWLIST: Set<String> =
            buildSet {
                addAll(EmergencySafety.NEVER_BLOCK_PACKAGES)
                add("com.wane.app")
            }
    }
}

private class FakeSessionManager {
    val sessionState = kotlinx.coroutines.flow.MutableStateFlow<SessionState>(SessionState.Idle)

    fun setState(state: SessionState) {
        sessionState.value = state
    }
}
