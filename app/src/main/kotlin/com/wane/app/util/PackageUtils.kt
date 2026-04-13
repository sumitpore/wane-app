package com.wane.app.util

object PackageUtils {

    fun getDialerPackages(): Set<String> = setOf(
        "com.android.dialer",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.samsung.android.incallui",
        "com.android.phone",
        "com.android.server.telecom",
        "com.oneplus.dialer",
        "com.asus.contacts",
        "com.huawei.contacts",
        "com.motorola.contacts",
        "com.lge.phone",
        "com.sonymobile.android.dialer",
        "com.android.incallui",
    )

    fun getContactsPackages(): Set<String> = setOf(
        "com.android.contacts",
        "com.google.android.contacts",
        "com.samsung.android.contacts",
        "com.oneplus.contacts",
        "com.huawei.contacts",
        "com.sonymobile.android.contacts",
    )

    fun getSmsPackages(): Set<String> = setOf(
        "com.android.mms",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.oneplus.mms",
        "com.huawei.message",
        "com.sonymobile.conversations",
    )
}
