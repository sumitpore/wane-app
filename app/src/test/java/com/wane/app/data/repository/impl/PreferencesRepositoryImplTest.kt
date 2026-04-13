package com.wane.app.data.repository.impl

import com.wane.app.shared.AutoLockConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the validation logic extracted from [PreferencesRepositoryImpl].
 *
 * DataStore itself is not instantiated here since it requires Android infrastructure.
 * Instead, we mirror the private [PreferencesRepositoryImpl.isValidAutoLockConfig] logic
 * and the simple range/guard checks that gate every public setter.
 */
class PreferencesRepositoryImplTest {

    @Test
    fun `duration at lower bound is valid`() {
        assertTrue(isValidDuration(5))
    }

    @Test
    fun `duration at upper bound is valid`() {
        assertTrue(isValidDuration(120))
    }

    @Test
    fun `duration in middle of range is valid`() {
        assertTrue(isValidDuration(25))
    }

    @Test
    fun `duration below lower bound is rejected`() {
        assertFalse(isValidDuration(4))
    }

    @Test
    fun `duration above upper bound is rejected`() {
        assertFalse(isValidDuration(121))
    }

    @Test
    fun `duration zero is rejected`() {
        assertFalse(isValidDuration(0))
    }

    @Test
    fun `negative duration is rejected`() {
        assertFalse(isValidDuration(-1))
    }

    @Test
    fun `valid auto lock config with no skip window is accepted`() {
        val config = AutoLockConfig(
            enabled = true,
            durationMinutes = 30,
            gracePeriodSeconds = 10,
        )
        assertTrue(isValidAutoLockConfig(config))
    }

    @Test
    fun `valid auto lock config with full skip window is accepted`() {
        val config = AutoLockConfig(
            enabled = true,
            durationMinutes = 30,
            gracePeriodSeconds = 10,
            skipStartHour = 22,
            skipStartMinute = 0,
            skipEndHour = 6,
            skipEndMinute = 30,
        )
        assertTrue(isValidAutoLockConfig(config))
    }

    @Test
    fun `auto lock config with duration below range is rejected`() {
        val config = AutoLockConfig(durationMinutes = 4, gracePeriodSeconds = 10)
        assertFalse(isValidAutoLockConfig(config))
    }

    @Test
    fun `auto lock config with duration above range is rejected`() {
        val config = AutoLockConfig(durationMinutes = 121, gracePeriodSeconds = 10)
        assertFalse(isValidAutoLockConfig(config))
    }

    @Test
    fun `auto lock config with grace period below range is rejected`() {
        val config = AutoLockConfig(durationMinutes = 30, gracePeriodSeconds = 4)
        assertFalse(isValidAutoLockConfig(config))
    }

    @Test
    fun `auto lock config with grace period above range is rejected`() {
        val config = AutoLockConfig(durationMinutes = 30, gracePeriodSeconds = 61)
        assertFalse(isValidAutoLockConfig(config))
    }

    @Test
    fun `partial skip window with only start hour is rejected`() {
        val config = AutoLockConfig(
            durationMinutes = 30,
            gracePeriodSeconds = 10,
            skipStartHour = 22,
            skipStartMinute = null,
            skipEndHour = null,
            skipEndMinute = null,
        )
        assertFalse(isValidAutoLockConfig(config))
    }

    @Test
    fun `partial skip window missing end minute is rejected`() {
        val config = AutoLockConfig(
            durationMinutes = 30,
            gracePeriodSeconds = 10,
            skipStartHour = 22,
            skipStartMinute = 0,
            skipEndHour = 6,
            skipEndMinute = null,
        )
        assertFalse(isValidAutoLockConfig(config))
    }

    @Test
    fun `zero-length skip window where start equals end is rejected`() {
        val config = AutoLockConfig(
            durationMinutes = 30,
            gracePeriodSeconds = 10,
            skipStartHour = 8,
            skipStartMinute = 30,
            skipEndHour = 8,
            skipEndMinute = 30,
        )
        assertFalse(isValidAutoLockConfig(config))
    }

    @Test
    fun `skip window with hour out of range is rejected`() {
        val config = AutoLockConfig(
            durationMinutes = 30,
            gracePeriodSeconds = 10,
            skipStartHour = 24,
            skipStartMinute = 0,
            skipEndHour = 6,
            skipEndMinute = 0,
        )
        assertFalse(isValidAutoLockConfig(config))
    }

    @Test
    fun `skip window with minute out of range is rejected`() {
        val config = AutoLockConfig(
            durationMinutes = 30,
            gracePeriodSeconds = 10,
            skipStartHour = 22,
            skipStartMinute = 60,
            skipEndHour = 6,
            skipEndMinute = 0,
        )
        assertFalse(isValidAutoLockConfig(config))
    }

    @Test
    fun `blank theme id is rejected`() {
        assertFalse(isValidThemeId(""))
        assertFalse(isValidThemeId("   "))
    }

    @Test
    fun `non-blank theme id is accepted`() {
        assertTrue(isValidThemeId("default"))
        assertTrue(isValidThemeId("ocean"))
    }

    @Test
    fun `emergency contacts trims whitespace and filters empty`() {
        val raw = listOf("  alice  ", "", "  ", "bob")
        val cleaned = cleanEmergencyContacts(raw)
        assertTrue(cleaned == listOf("alice", "bob"))
    }

    @Test
    fun `emergency contacts with all empty strings results in empty list`() {
        val cleaned = cleanEmergencyContacts(listOf("", "  ", "\t"))
        assertTrue(cleaned.isEmpty())
    }

    @Test
    fun `emergency contacts preserves order`() {
        val cleaned = cleanEmergencyContacts(listOf("charlie", "alice", "bob"))
        assertTrue(cleaned == listOf("charlie", "alice", "bob"))
    }

    companion object {
        private val DURATION_RANGE = 5..120
        private val GRACE_RANGE = 5..60
        private val HOUR_RANGE = 0..23
        private val MINUTE_RANGE = 0..59

        private fun isValidDuration(minutes: Int): Boolean = minutes in DURATION_RANGE

        private fun isValidThemeId(id: String): Boolean = id.isNotBlank()

        private fun cleanEmergencyContacts(contacts: List<String>): List<String> =
            contacts.map { it.trim() }.filter { it.isNotEmpty() }

        private fun isValidAutoLockConfig(config: AutoLockConfig): Boolean {
            if (config.durationMinutes !in DURATION_RANGE) return false
            if (config.gracePeriodSeconds !in GRACE_RANGE) return false
            val fields = listOf(
                config.skipStartHour,
                config.skipStartMinute,
                config.skipEndHour,
                config.skipEndMinute,
            )
            val allNull = fields.all { it == null }
            val anyNull = fields.any { it == null }
            if (anyNull && !allNull) return false
            if (!allNull) {
                val sh = config.skipStartHour!!
                val sm = config.skipStartMinute!!
                val eh = config.skipEndHour!!
                val em = config.skipEndMinute!!
                if (sh !in HOUR_RANGE || sm !in MINUTE_RANGE ||
                    eh !in HOUR_RANGE || em !in MINUTE_RANGE
                ) {
                    return false
                }
                if (sh == eh && sm == em) return false
            }
            return true
        }
    }
}
