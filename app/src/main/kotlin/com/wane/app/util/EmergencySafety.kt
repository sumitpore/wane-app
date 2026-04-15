package com.wane.app.util

object EmergencySafety {

    val NEVER_BLOCK_PACKAGES: Set<String> = setOf(
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

    fun isNeverBlockPackage(packageName: String): Boolean {
        return packageName in NEVER_BLOCK_PACKAGES
    }
}
