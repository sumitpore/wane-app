package com.wane.app.util

object EmergencySafety {

    val EMERGENCY_NUMBERS: Set<String> = setOf(
        "911",
        "112",
        "999",
        "000",
        "110",
        "119",
        "08",
        "118",
        "102",
        "103",
    )

    val NEVER_BLOCK_PACKAGES: Set<String> = setOf(
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.phone",
        "com.android.server.telecom",
        "com.android.systemui",
        "com.android.settings",
        "com.android.emergency",
    )

    fun isEmergencyNumber(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        return EMERGENCY_NUMBERS.any { emergency ->
            digits.endsWith(emergency)
        }
    }

    fun isNeverBlockPackage(packageName: String): Boolean {
        return packageName in NEVER_BLOCK_PACKAGES
    }
}
