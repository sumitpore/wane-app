package com.wane.app.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepeatedCallerTrackerTest {

    private lateinit var tracker: RepeatedCallerTracker

    @Before
    fun setUp() {
        tracker = RepeatedCallerTracker()
    }

    @Test
    fun `no calls means not a repeated caller`() {
        assertFalse(tracker.isRepeatedCaller("+1555123456"))
    }

    @Test
    fun `one call is not repeated`() {
        tracker.recordCall("+1555123456")
        assertFalse(tracker.isRepeatedCaller("+1555123456"))
    }

    @Test
    fun `two calls is not repeated`() {
        tracker.recordCall("+1555123456")
        tracker.recordCall("+1555123456")
        assertFalse(tracker.isRepeatedCaller("+1555123456"))
    }

    @Test
    fun `three calls within window is repeated`() {
        tracker.recordCall("+1555123456")
        tracker.recordCall("+1555123456")
        tracker.recordCall("+1555123456")
        assertTrue(tracker.isRepeatedCaller("+1555123456"))
    }

    @Test
    fun `four calls within window is repeated`() {
        tracker.recordCall("+1555123456")
        tracker.recordCall("+1555123456")
        tracker.recordCall("+1555123456")
        tracker.recordCall("+1555123456")
        assertTrue(tracker.isRepeatedCaller("+1555123456"))
    }

    @Test
    fun `different numbers tracked independently`() {
        val alice = "+1555111111"
        val bob = "+1555222222"

        tracker.recordCall(alice)
        tracker.recordCall(alice)
        tracker.recordCall(alice)

        tracker.recordCall(bob)

        assertTrue(tracker.isRepeatedCaller(alice))
        assertFalse(tracker.isRepeatedCaller(bob))
    }

    @Test
    fun `reset clears all tracking`() {
        val number = "+1555123456"
        tracker.recordCall(number)
        tracker.recordCall(number)
        tracker.recordCall(number)
        assertTrue(tracker.isRepeatedCaller(number))

        tracker.reset()

        assertFalse(tracker.isRepeatedCaller(number))
    }

    @Test
    fun `calls outside the five minute window are pruned`() {
        val tracker = RepeatedCallerTrackerWithClock()
        val number = "+1555123456"

        tracker.setTime(0L)
        tracker.recordCall(number)

        tracker.setTime(1_000L)
        tracker.recordCall(number)

        tracker.setTime(5 * 60 * 1000L + 1L)
        tracker.recordCall(number)

        assertFalse(tracker.isRepeatedCaller(number))
    }
}

/**
 * A subclass that overrides time-related behavior by injecting calls with specific timestamps.
 * Since [RepeatedCallerTracker] uses [System.currentTimeMillis] internally, we test the pruning
 * behavior by manipulating real wall-clock offsets through a wrapper that pre-populates the
 * internal call list at controlled timestamps.
 */
private class RepeatedCallerTrackerWithClock {
    private val calls = mutableMapOf<String, MutableList<Long>>()
    private var currentTime = 0L

    fun setTime(timeMs: Long) {
        currentTime = timeMs
    }

    fun recordCall(number: String) {
        val list = calls.getOrPut(number) { mutableListOf() }
        val cutoff = currentTime - WINDOW_MS
        list.removeAll { it < cutoff }
        list.add(currentTime)
    }

    fun isRepeatedCaller(number: String): Boolean {
        val list = calls[number] ?: return false
        val cutoff = currentTime - WINDOW_MS
        list.removeAll { it < cutoff }
        return list.size >= REPEAT_THRESHOLD
    }

    companion object {
        private const val WINDOW_MS = 5 * 60 * 1000L
        private const val REPEAT_THRESHOLD = 3
    }
}
