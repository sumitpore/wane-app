package com.unclutteredapps.wane.util

object SystemPackages {
    val NEVER_BLOCK: Set<String> =
        setOf(
            "android",
            "com.android.internal.app",
            "com.android.systemui",
            "com.android.settings",
            "com.android.emergency",
            "com.android.phone",
            "com.android.server.telecom",
            "com.android.providers.contacts",
            "com.android.dialer",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.google.android.permissioncontroller",
            "com.samsung.android.app.resolver",
        )

    fun isNeverBlock(packageName: String): Boolean = packageName in NEVER_BLOCK
}
