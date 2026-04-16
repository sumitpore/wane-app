package com.wane.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.telecom.TelecomManager
import android.view.inputmethod.InputMethodManager

/**
 * Resolves allowed packages entirely via Android runtime APIs — no hardcoded
 * package-name lists. Each `resolve*` function queries the OS for whichever
 * apps currently hold the relevant role, handle the relevant intents, or are
 * enabled by the user.
 */
object PackageUtils {
    fun resolveDialerPackages(context: Context): Set<String> =
        buildSet {
            resolveDefaultDialer(context)?.let { add(it) }
            addAll(resolvePackages(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))))
            addAll(resolvePackages(context, Intent(Intent.ACTION_CALL, Uri.parse("tel:"))))
            addAll(resolveInCallServices(context))
        }

    fun resolveContactsPackages(context: Context): Set<String> =
        buildSet {
            addAll(resolvePackages(context, Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)))
            addAll(resolvePackages(context, Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)))
            addAll(resolvePackages(context, Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)))
            addAll(resolvePackages(context, Intent(Intent.ACTION_EDIT, ContactsContract.Contacts.CONTENT_URI)))
        }

    fun resolveSmsPackages(context: Context): Set<String> =
        buildSet {
            resolveDefaultSmsPackage(context)?.let { add(it) }
        }

    fun resolveImePackages(context: Context): Set<String> =
        buildSet {
            try {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.enabledInputMethodList?.forEach { add(it.packageName) }
            } catch (_: Exception) {
                // Empty — the AccessibilityService has its own live IME check as a fallback
            }
        }

    private fun resolvePackages(
        context: Context,
        intent: Intent,
    ): List<String> =
        context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { it.activityInfo?.packageName }

    private fun resolveInCallServices(context: Context): List<String> =
        context.packageManager
            .queryIntentServices(Intent("android.telecom.InCallService"), PackageManager.MATCH_ALL)
            .mapNotNull { it.serviceInfo?.packageName }

    private fun resolveDefaultDialer(context: Context): String? =
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            telecomManager?.defaultDialerPackage
        } catch (_: Exception) {
            null
        }

    private fun resolveDefaultSmsPackage(context: Context): String? =
        try {
            Telephony.Sms.getDefaultSmsPackage(context)
        } catch (_: Exception) {
            null
        }
}
