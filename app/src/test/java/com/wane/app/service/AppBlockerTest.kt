package com.wane.app.service

import com.wane.app.shared.SessionState
import com.wane.app.util.EmergencySafety
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

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
 *   - full-screen exemption add / remove / clear / expiry
 */
class AppBlockerTest {
    private val fakeSessionManager = FakeSessionManager()
    private val blocker = AppBlockerShim(fakeSessionManager)

    @Before
    fun setUp() {
        blocker.clearFullScreenExemptions()
        fakeSessionManager.setState(SessionState.Idle)
    }

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

    @Test
    fun `never-block packages are not blocked even without full-screen exemption`() {
        fakeSessionManager.setState(runningState())
        for (pkg in EmergencySafety.NEVER_BLOCK_PACKAGES) {
            assertFalse("$pkg should pass without exemption", blocker.shouldBlockApp(pkg))
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

    // ── Full-screen exemption ─────────────────────────────────────────

    @Test
    fun `running state does not block a full-screen exempt package`() {
        fakeSessionManager.setState(runningState())
        blocker.addFullScreenExemption("0|com.phone.app|111", "com.phone.app")
        assertFalse(blocker.shouldBlockApp("com.phone.app"))
    }

    @Test
    fun `full-screen exemption removed then package is blocked`() {
        fakeSessionManager.setState(runningState())
        blocker.addFullScreenExemption("0|com.phone.app|111", "com.phone.app")
        assertFalse(blocker.shouldBlockApp("com.phone.app"))

        blocker.removeFullScreenExemption("0|com.phone.app|111")
        assertTrue(blocker.shouldBlockApp("com.phone.app"))
    }

    @Test
    fun `clearFullScreenExemptions then previously exempt package is blocked`() {
        fakeSessionManager.setState(runningState())
        blocker.addFullScreenExemption("key_a", "com.phone.app")
        blocker.addFullScreenExemption("key_b", "com.alarm.app")
        assertFalse(blocker.shouldBlockApp("com.phone.app"))
        assertFalse(blocker.shouldBlockApp("com.alarm.app"))

        blocker.clearFullScreenExemptions()
        assertTrue(blocker.shouldBlockApp("com.phone.app"))
        assertTrue(blocker.shouldBlockApp("com.alarm.app"))
    }

    @Test
    fun `expired full-screen exemption does not prevent blocking`() {
        var fakeNow = 1_000_000L
        val expiryBlocker = AppBlockerShim(fakeSessionManager, clock = { fakeNow })
        fakeSessionManager.setState(runningState())

        expiryBlocker.addFullScreenExemption("key_call", "com.phone.app")
        assertFalse(expiryBlocker.shouldBlockApp("com.phone.app"))

        fakeNow = 1_000_000L + AppBlockerShim.FULL_SCREEN_EXEMPTION_TTL_MS + 1
        assertTrue(expiryBlocker.shouldBlockApp("com.phone.app"))
    }

    @Test
    fun `multiple notifications from same package - removing one keeps exemption`() {
        fakeSessionManager.setState(runningState())
        blocker.addFullScreenExemption("key_notif_1", "com.phone.app")
        blocker.addFullScreenExemption("key_notif_2", "com.phone.app")
        assertFalse(blocker.shouldBlockApp("com.phone.app"))

        blocker.removeFullScreenExemption("key_notif_1")
        assertFalse(
            "Second exemption should still protect the package",
            blocker.shouldBlockApp("com.phone.app"),
        )

        blocker.removeFullScreenExemption("key_notif_2")
        assertTrue(blocker.shouldBlockApp("com.phone.app"))
    }

    @Test
    fun `full-screen exemption does not affect other packages`() {
        fakeSessionManager.setState(runningState())
        blocker.addFullScreenExemption("key_call", "com.phone.app")

        assertFalse(blocker.shouldBlockApp("com.phone.app"))
        assertTrue(
            "Unrelated package should still be blocked",
            blocker.shouldBlockApp("com.twitter.android"),
        )
        assertTrue(blocker.shouldBlockApp("com.instagram.android"))
    }

    @Test
    fun `non-running state ignores full-screen exemption`() {
        fakeSessionManager.setState(SessionState.Idle)
        blocker.addFullScreenExemption("key_call", "com.twitter.android")
        assertFalse(blocker.shouldBlockApp("com.twitter.android"))

        fakeSessionManager.setState(SessionState.EmergencyExit)
        assertFalse(blocker.shouldBlockApp("com.twitter.android"))
    }

    @Test
    fun `expired entry is lazily evicted on next check`() {
        var fakeNow = 1_000_000L
        val expiryBlocker = AppBlockerShim(fakeSessionManager, clock = { fakeNow })
        fakeSessionManager.setState(runningState())

        expiryBlocker.addFullScreenExemption("key_old", "com.old.app")
        expiryBlocker.addFullScreenExemption("key_new", "com.new.app")

        fakeNow = 1_000_000L + AppBlockerShim.FULL_SCREEN_EXEMPTION_TTL_MS + 1
        expiryBlocker.addFullScreenExemption("key_fresh", "com.fresh.app")

        assertTrue("Expired exemption should not protect", expiryBlocker.shouldBlockApp("com.old.app"))
        assertTrue(expiryBlocker.shouldBlockApp("com.new.app"))
        assertFalse("Fresh exemption should protect", expiryBlocker.shouldBlockApp("com.fresh.app"))
    }

    @Test
    fun `notification update does not refresh exemption TTL`() {
        var fakeNow = 1_000_000L
        val expiryBlocker = AppBlockerShim(fakeSessionManager, clock = { fakeNow })
        fakeSessionManager.setState(runningState())

        expiryBlocker.addFullScreenExemption("key_call", "com.phone.app")
        assertFalse(expiryBlocker.shouldBlockApp("com.phone.app"))

        fakeNow = 1_000_000L + 50_000L
        expiryBlocker.addFullScreenExemption("key_call", "com.phone.app")

        fakeNow = 1_000_000L + AppBlockerShim.FULL_SCREEN_EXEMPTION_TTL_MS + 1
        assertTrue(
            "Update should not have refreshed TTL",
            expiryBlocker.shouldBlockApp("com.phone.app"),
        )
    }

    @Test
    fun `exemption just before expiry still protects`() {
        var fakeNow = 1_000_000L
        val expiryBlocker = AppBlockerShim(fakeSessionManager, clock = { fakeNow })
        fakeSessionManager.setState(runningState())

        expiryBlocker.addFullScreenExemption("key_call", "com.phone.app")

        fakeNow = 1_000_000L + AppBlockerShim.FULL_SCREEN_EXEMPTION_TTL_MS
        assertFalse(
            "Exemption at exact TTL boundary should still protect",
            expiryBlocker.shouldBlockApp("com.phone.app"),
        )
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
 *
 * The [clock] parameter replaces [android.os.SystemClock.elapsedRealtime] so
 * tests can control time deterministically.
 */
private class AppBlockerShim(
    private val sessionManager: FakeSessionManager,
    private val clock: () -> Long = { 1_000_000L },
) {
    private val fullScreenExemptions = ConcurrentHashMap<String, FullScreenExemption>()

    fun shouldBlockApp(packageName: String): Boolean {
        if (EmergencySafety.isNeverBlockPackage(packageName)) return false
        if (sessionManager.sessionState.value !is SessionState.Running) return false
        if (packageName in STATIC_ALLOWLIST) return false
        if (isFullScreenExempt(packageName)) return false
        return true
    }

    fun addFullScreenExemption(
        notificationKey: String,
        packageName: String,
    ) {
        val expiration = clock() + FULL_SCREEN_EXEMPTION_TTL_MS
        fullScreenExemptions.putIfAbsent(notificationKey, FullScreenExemption(packageName, expiration))
    }

    fun removeFullScreenExemption(notificationKey: String) {
        fullScreenExemptions.remove(notificationKey)
    }

    fun clearFullScreenExemptions() {
        fullScreenExemptions.clear()
    }

    private fun isFullScreenExempt(packageName: String): Boolean {
        val now = clock()
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

    private data class FullScreenExemption(
        val packageName: String,
        val expirationElapsedMs: Long,
    )

    companion object {
        const val FULL_SCREEN_EXEMPTION_TTL_MS = 60_000L
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
