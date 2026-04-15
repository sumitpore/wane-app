package com.wane.app.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object AccessibilityUtils {
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponent =
            ComponentName(
                context.packageName,
                "com.wane.app.service.WaneAccessibilityService",
            )
        val enabledServices =
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            val enabledComponent = ComponentName.unflattenFromString(splitter.next())
            if (expectedComponent == enabledComponent) return true
        }
        return false
    }
}
