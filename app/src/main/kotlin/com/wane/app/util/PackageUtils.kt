package com.wane.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract

object PackageUtils {

    private val DIALER_PACKAGES_FALLBACK: Set<String> = setOf(
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

    private val CONTACTS_PACKAGES_FALLBACK: Set<String> = setOf(
        "com.android.contacts",
        "com.google.android.contacts",
        "com.samsung.android.contacts",
        "com.oneplus.contacts",
        "com.huawei.contacts",
        "com.sonymobile.android.contacts",
    )

    private val SMS_PACKAGES_FALLBACK: Set<String> = setOf(
        "com.android.mms",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.oneplus.mms",
        "com.huawei.message",
        "com.sonymobile.conversations",
    )

    fun getDialerPackages(): Set<String> = DIALER_PACKAGES_FALLBACK

    fun getContactsPackages(): Set<String> = CONTACTS_PACKAGES_FALLBACK

    fun getSmsPackages(): Set<String> = SMS_PACKAGES_FALLBACK

    fun resolveDialerPackages(context: Context): Set<String> = buildSet {
        addAll(DIALER_PACKAGES_FALLBACK)
        addAll(resolvePackages(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))))
        addAll(resolvePackages(context, Intent(Intent.ACTION_CALL, Uri.parse("tel:"))))
    }

    fun resolveContactsPackages(context: Context): Set<String> = buildSet {
        addAll(CONTACTS_PACKAGES_FALLBACK)
        addAll(resolvePackages(context, Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)))
        addAll(resolvePackages(context, Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)))
    }

    fun resolveSmsPackages(context: Context): Set<String> = buildSet {
        addAll(SMS_PACKAGES_FALLBACK)
        addAll(resolvePackages(context, Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))))
        addAll(resolvePackages(context, Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))))
    }

    private fun resolvePackages(context: Context, intent: Intent): List<String> {
        return context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { it.activityInfo?.packageName }
    }
}
