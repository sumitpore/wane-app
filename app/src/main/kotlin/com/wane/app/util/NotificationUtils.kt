package com.wane.app.util

import android.app.Notification
import android.net.Uri
import android.os.Build
import android.service.notification.StatusBarNotification

object NotificationUtils {

    fun extractCallerNumber(sbn: StatusBarNotification): String? {
        val extras = sbn.notification.extras ?: return null

        val people: java.util.ArrayList<android.app.Person>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelableArrayList(
                    Notification.EXTRA_PEOPLE_LIST,
                    android.app.Person::class.java,
                )
            } else {
                @Suppress("DEPRECATION")
                extras.getParcelableArrayList(Notification.EXTRA_PEOPLE_LIST)
            }
        people?.forEach { person ->
            val uri = person.uri
            if (uri != null) {
                extractNumberFromTelUri(uri)?.let { return it }
            }
        }

        extras.getString("android.telecom.extra.CALL_NUMBER")?.let { number ->
            if (number.isNotBlank()) return number.trim()
        }

        extras.getString("android.intent.extra.PHONE_NUMBER")?.let { number ->
            if (number.isNotBlank()) return number.trim()
        }

        extras.getString("address")?.let { address ->
            if (address.isNotBlank() && address.any { it.isDigit() }) return address.trim()
        }

        return null
    }

    fun isPhoneNotification(sbn: StatusBarNotification): Boolean {
        return sbn.packageName in PackageUtils.getDialerPackages()
    }

    private fun extractNumberFromTelUri(uriString: String): String? {
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme.equals("tel", ignoreCase = true)) {
                uri.schemeSpecificPart?.trim()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
