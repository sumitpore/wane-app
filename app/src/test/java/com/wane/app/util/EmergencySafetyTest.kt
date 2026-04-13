package com.wane.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmergencySafetyTest {

    @Test
    fun `911 is emergency number`() {
        assertTrue(EmergencySafety.isEmergencyNumber("911"))
    }

    @Test
    fun `112 is emergency number`() {
        assertTrue(EmergencySafety.isEmergencyNumber("112"))
    }

    @Test
    fun `999 is emergency number`() {
        assertTrue(EmergencySafety.isEmergencyNumber("999"))
    }

    @Test
    fun `formatted number with country code and dashes is recognized`() {
        assertTrue(EmergencySafety.isEmergencyNumber("+1-911"))
    }

    @Test
    fun `number with parentheses and spaces is recognized`() {
        assertTrue(EmergencySafety.isEmergencyNumber("(+1) 911"))
    }

    @Test
    fun `longer number ending in emergency suffix matches`() {
        assertTrue(EmergencySafety.isEmergencyNumber("1-800-555-0911"))
    }

    @Test
    fun `all known emergency numbers are recognized`() {
        for (number in EmergencySafety.EMERGENCY_NUMBERS) {
            assertTrue(
                "Expected $number to be recognized as emergency",
                EmergencySafety.isEmergencyNumber(number),
            )
        }
    }

    @Test
    fun `regular phone number is not emergency`() {
        assertFalse(EmergencySafety.isEmergencyNumber("12345"))
    }

    @Test
    fun `empty string is not emergency`() {
        assertFalse(EmergencySafety.isEmergencyNumber(""))
    }

    @Test
    fun `non-digit string is not emergency`() {
        assertFalse(EmergencySafety.isEmergencyNumber("abc"))
    }

    @Test
    fun `known never-block packages are identified`() {
        for (pkg in EmergencySafety.NEVER_BLOCK_PACKAGES) {
            assertTrue(
                "Expected $pkg to be a never-block package",
                EmergencySafety.isNeverBlockPackage(pkg),
            )
        }
    }

    @Test
    fun `unknown package is not never-block`() {
        assertFalse(EmergencySafety.isNeverBlockPackage("com.twitter.android"))
    }

    @Test
    fun `empty package name is not never-block`() {
        assertFalse(EmergencySafety.isNeverBlockPackage(""))
    }

    @Test
    fun `emergency numbers set contains expected entries`() {
        val expected = setOf("911", "112", "999", "000", "110", "119", "118", "102", "103")
        assertTrue(EmergencySafety.EMERGENCY_NUMBERS.containsAll(expected))
    }

    @Test
    fun `never-block packages set contains expected entries`() {
        val expected = setOf(
            "com.android.dialer",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.android.phone",
            "com.android.server.telecom",
            "com.android.systemui",
            "com.android.settings",
            "com.android.emergency",
        )
        assertTrue(EmergencySafety.NEVER_BLOCK_PACKAGES.containsAll(expected))
    }

    @Test
    fun `emergency numbers set has expected count`() {
        val snapshot = EmergencySafety.EMERGENCY_NUMBERS.toSet()
        assertTrue(snapshot.size == 9)
        assertTrue(snapshot == EmergencySafety.EMERGENCY_NUMBERS)
    }

    @Test
    fun `never-block packages set has expected count`() {
        val snapshot = EmergencySafety.NEVER_BLOCK_PACKAGES.toSet()
        assertTrue(snapshot.size == 8)
        assertTrue(snapshot == EmergencySafety.NEVER_BLOCK_PACKAGES)
    }
}
