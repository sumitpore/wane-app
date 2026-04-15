package com.wane.app.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

object NotificationListenerUtils {
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val expectedComponent =
            ComponentName(
                context.packageName,
                "com.wane.app.service.WaneNotificationListener",
            )
        val enabledListeners =
            Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners",
            ) ?: return false

        return enabledListeners.split(":").any { flat ->
            ComponentName.unflattenFromString(flat) == expectedComponent
        }
    }
}
