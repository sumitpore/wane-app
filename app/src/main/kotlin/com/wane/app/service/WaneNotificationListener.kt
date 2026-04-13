package com.wane.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.wane.app.data.repository.PreferencesRepository
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
import kotlinx.coroutines.launch

class WaneNotificationListener : NotificationListenerService() {

    private lateinit var sessionManager: SessionManager
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var repeatedCallerTracker: RepeatedCallerTracker

    private var serviceScope: CoroutineScope? = null
    private val snoozedKeys = mutableSetOf<String>()
    private var emergencyContacts: List<String> = emptyList()

    private val phoneAndSmsPackages: Set<String> by lazy {
        buildSet {
            addAll(PackageUtils.getDialerPackages())
            addAll(PackageUtils.getContactsPackages())
            addAll(PackageUtils.getSmsPackages())
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                NotificationListenerEntryPoint::class.java,
            )
            sessionManager = entryPoint.sessionManager()
            preferencesRepository = entryPoint.preferencesRepository()
            repeatedCallerTracker = entryPoint.repeatedCallerTracker()

            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            serviceScope = scope

            scope.launch {
                preferencesRepository.observeEmergencyContacts().collectLatest { contacts ->
                    emergencyContacts = contacts
                }
            }

            scope.launch {
                sessionManager.sessionState.collectLatest { state ->
                    if (state !is SessionState.Running) {
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

            if (callerNumber != null && EmergencySafety.isEmergencyNumber(callerNumber)) return

            if (callerNumber != null && isEmergencyContact(callerNumber)) return

            if (callerNumber != null) {
                repeatedCallerTracker.recordCall(callerNumber)
                if (repeatedCallerTracker.isRepeatedCaller(callerNumber)) {
                    showRepeatedCallerNotification(callerNumber)
                    return
                }
            }

            if (sessionManager.sessionState.value !is SessionState.Running) return

            if (sbn.packageName in phoneAndSmsPackages) return

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
        } catch (e: Exception) {
            Log.e(TAG, "onNotificationRemoved error", e)
        }
    }

    private fun isEmergencyContact(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        return emergencyContacts.any { contact ->
            val contactDigits = contact.filter { it.isDigit() }
            contactDigits.isNotEmpty() && (digits.endsWith(contactDigits) || contactDigits.endsWith(digits))
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
    }

    private fun showRepeatedCallerNotification(callerNumber: String) {
        try {
            val nm = getSystemService(NotificationManager::class.java) ?: return
            val channel = NotificationChannel(
                CHANNEL_REPEATED_CALLER,
                "Repeated Caller Alerts",
                NotificationManager.IMPORTANCE_HIGH,
            )
            nm.createNotificationChannel(channel)

            val maskedNumber = if (callerNumber.length > 4) {
                "***${callerNumber.takeLast(4)}"
            } else {
                callerNumber
            }

            val notification = Notification.Builder(this, CHANNEL_REPEATED_CALLER)
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
    }
}
