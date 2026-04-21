package com.unclutteredapps.wane.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemPackagesTest {
    @Test
    fun `known never-block packages are identified`() {
        for (pkg in SystemPackages.NEVER_BLOCK) {
            assertTrue(
                "Expected $pkg to be a never-block package",
                SystemPackages.isNeverBlock(pkg),
            )
        }
    }

    @Test
    fun `unknown package is not never-block`() {
        assertFalse(SystemPackages.isNeverBlock("com.twitter.android"))
    }

    @Test
    fun `empty package name is not never-block`() {
        assertFalse(SystemPackages.isNeverBlock(""))
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
        assertTrue(SystemPackages.NEVER_BLOCK.containsAll(expected))
    }

    @Test
    fun `never-block packages set has expected count`() {
        val snapshot = SystemPackages.NEVER_BLOCK.toSet()
        assertTrue(snapshot.size == 13)
        assertTrue(snapshot == SystemPackages.NEVER_BLOCK)
    }
}
