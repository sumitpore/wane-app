package com.wane.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmergencySafetyTest {
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
    fun `never-block packages set contains expected entries`() {
        val expected =
            setOf(
                "com.android.dialer",
                "com.google.android.dialer",
                "com.samsung.android.dialer",
                "com.android.phone",
                "com.android.server.telecom",
                "com.android.systemui",
                "com.android.settings",
                "com.android.emergency",
                "android",
                "com.android.internal.app",
                "com.google.android.permissioncontroller",
                "com.samsung.android.app.resolver",
            )
        assertTrue(EmergencySafety.NEVER_BLOCK_PACKAGES.containsAll(expected))
    }

    @Test
    fun `never-block packages set has expected count`() {
        val snapshot = EmergencySafety.NEVER_BLOCK_PACKAGES.toSet()
        assertTrue(snapshot.size == 12)
        assertTrue(snapshot == EmergencySafety.NEVER_BLOCK_PACKAGES)
    }
}
