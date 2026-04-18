package com.wane.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.wane.app.service.di.NotificationListenerEntryPoint
import com.wane.app.shared.SessionState
import com.wane.app.util.EmergencySafety
import com.wane.app.util.NotificationUtils
import com.wane.app.util.PackageUtils
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WaneNotificationListener : NotificationListenerService() {
    private lateinit var sessionManager: SessionManager
    private lateinit var repeatedCallerTracker: RepeatedCallerTracker
    private lateinit var appBlocker: AppBlocker

    private var serviceScope: CoroutineScope? = null
    private val snoozedKeys = mutableSetOf<String>()

    private val phoneAndSmsPackages: Set<String> by lazy {
        buildSet {
            addAll(PackageUtils.resolveDialerPackages(applicationContext))
            addAll(PackageUtils.resolveContactsPackages(applicationContext))
            addAll(PackageUtils.resolveSmsPackages(applicationContext))
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            val entryPoint =
                EntryPointAccessors.fromApplication(
                    applicationContext,
                    NotificationListenerEntryPoint::class.java,
                )
            sessionManager = entryPoint.sessionManager()
            repeatedCallerTracker = entryPoint.repeatedCallerTracker()
            appBlocker = entryPoint.appBlocker()

            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            serviceScope = scope

            scope.launch {
                sessionManager.sessionState
                    .map { it is SessionState.Running }
                    .distinctUntilChanged()
                    .collectLatest { isRunning ->
                        if (isRunning) {
                            snoozeExistingNotifications()
                        } else {
                            unsnoozeAll()
                        }
                    }
            }

            Log.d(TAG, "Notification listener connected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize notification listener", e)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            unsnoozeAll()
            serviceScope?.cancel()
            serviceScope = null
            Log.d(TAG, "Notification listener disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "onListenerDisconnected error", e)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        try {
            sbn ?: return
            if (!::sessionManager.isInitialized) return

            val callerNumber = NotificationUtils.extractCallerNumber(sbn)

            if (callerNumber != null) {
                repeatedCallerTracker.recordCall(callerNumber)
                if (repeatedCallerTracker.isRepeatedCaller(callerNumber)) {
                    showRepeatedCallerNotification(callerNumber)
                    return
                }
            }

            if (sessionManager.sessionState.value !is SessionState.Running) return

            if (sbn.packageName in phoneAndSmsPackages) return
            if (sbn.packageName in EmergencySafety.NEVER_BLOCK_PACKAGES) return
            if (sbn.packageName == "com.wane.app") return
            if (sbn.notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0) return

            if (sbn.notification.fullScreenIntent != null) {
                val category = sbn.notification.category
                if (category in FULL_SCREEN_EXEMPT_CATEGORIES) {
                    appBlocker.addFullScreenExemption(sbn.key, sbn.packageName)
                    return
                }
            }

            try {
                snoozeNotification(sbn.key, Long.MAX_VALUE)
                synchronized(snoozedKeys) {
                    snoozedKeys.add(sbn.key)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to snooze notification ${sbn.key}", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onNotificationPosted error", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        try {
            sbn ?: return
            synchronized(snoozedKeys) {
                snoozedKeys.remove(sbn.key)
            }
            if (::appBlocker.isInitialized) {
                appBlocker.removeFullScreenExemption(sbn.key)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onNotificationRemoved error", e)
        }
    }

    private fun unsnoozeAll() {
        val keysToUnsnooze: Set<String>
        synchronized(snoozedKeys) {
            keysToUnsnooze = snoozedKeys.toSet()
            snoozedKeys.clear()
        }
        for (key in keysToUnsnooze) {
            try {
                snoozeNotification(key, 1L)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unsnooze notification $key", e)
            }
        }
        if (::appBlocker.isInitialized) {
            appBlocker.clearFullScreenExemptions()
        }
    }

    private fun shouldSnooze(sbn: StatusBarNotification): Boolean =
        sbn.packageName !in phoneAndSmsPackages &&
            sbn.packageName !in EmergencySafety.NEVER_BLOCK_PACKAGES &&
            sbn.packageName != "com.wane.app" &&
            sbn.notification.flags and Notification.FLAG_FOREGROUND_SERVICE == 0 &&
            sbn.notification.fullScreenIntent == null

    private fun snoozeExistingNotifications() {
        try {
            val active = getActiveNotifications() ?: return
            for (sbn in active) {
                if (!shouldSnooze(sbn)) continue
                try {
                    snoozeNotification(sbn.key, Long.MAX_VALUE)
                    synchronized(snoozedKeys) {
                        snoozedKeys.add(sbn.key)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to snooze existing notification ${sbn.key}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "snoozeExistingNotifications error", e)
        }
    }

    private fun showRepeatedCallerNotification(callerNumber: String) {
        try {
            val nm = getSystemService(NotificationManager::class.java) ?: return
            val channel =
                NotificationChannel(
                    CHANNEL_REPEATED_CALLER,
                    "Repeated Caller Alerts",
                    NotificationManager.IMPORTANCE_HIGH,
                )
            nm.createNotificationChannel(channel)

            val maskedNumber =
                if (callerNumber.length > 4) {
                    "***${callerNumber.takeLast(4)}"
                } else {
                    callerNumber
                }

            val notification =
                Notification
                    .Builder(this, CHANNEL_REPEATED_CALLER)
                    .setSmallIcon(android.R.drawable.ic_menu_call)
                    .setContentTitle("Repeated caller")
                    .setContentText("$maskedNumber called 3+ times in 5 minutes")
                    .setAutoCancel(true)
                    .build()

            nm.notify(NOTIFICATION_ID_REPEATED_CALLER, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show repeated caller notification", e)
        }
    }

    companion object {
        private const val TAG = "WaneNotifListener"
        private const val CHANNEL_REPEATED_CALLER = "repeated_caller"
        private const val NOTIFICATION_ID_REPEATED_CALLER = 9001

        private val FULL_SCREEN_EXEMPT_CATEGORIES =
            setOf(
                Notification.CATEGORY_CALL,
                Notification.CATEGORY_ALARM,
                Notification.CATEGORY_REMINDER,
                Notification.CATEGORY_NAVIGATION,
            )
    }
}
