package com.wane.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.wane.app.MainActivity
import com.wane.app.R
import com.wane.app.shared.SessionState
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WaneSessionService : LifecycleService() {

    @Inject
    lateinit var sessionManager: SessionManager

    private var notificationUpdateJob: Job? = null
    private var channelCreated = false

    override fun onCreate() {
        super.onCreate()
        try {
            ensureNotificationChannel()
        } catch (e: Exception) {
            Log.e(TAG, "onCreate channel", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return try {
            when (intent?.action) {
                ACTION_STOP -> {
                    notificationUpdateJob?.cancel()
                    notificationUpdateJob = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        stopForeground(Service.STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }
                    stopSelf()
                    START_NOT_STICKY
                }
                ACTION_START, null -> {
                    ensureNotificationChannel()
                    val running = sessionManager.sessionState.value as? SessionState.Running
                    val remainingMs = running?.remainingMs ?: 0L
                    startForeground(
                        NOTIFICATION_ID,
                        buildNotification(remainingMs),
                    )
                    startNotificationUpdates()
                    START_STICKY
                }
                else -> START_STICKY
            }
        } catch (e: Exception) {
            Log.e(TAG, "onStartCommand failed", e)
            START_STICKY
        }
    }

    override fun onDestroy() {
        try {
            notificationUpdateJob?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy cancel job", e)
        }
        notificationUpdateJob = null
        super.onDestroy()
    }

    private fun ensureNotificationChannel() {
        if (channelCreated) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            )
            mgr.createNotificationChannel(channel)
        }
        channelCreated = true
    }

    private fun startNotificationUpdates() {
        notificationUpdateJob?.cancel()
        notificationUpdateJob = lifecycleScope.launch {
            while (isActive) {
                delay(NOTIFICATION_UPDATE_INTERVAL_MS)
                try {
                    when (val s = sessionManager.sessionState.value) {
                        is SessionState.Running -> {
                            val mgr = getSystemService(NotificationManager::class.java)
                            mgr.notify(NOTIFICATION_ID, buildNotification(s.remainingMs))
                        }
                        else -> Unit
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "notification tick failed", e)
                }
            }
        }
    }

    private fun buildNotification(remainingMs: Long): Notification {
        val launch = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            this,
            0,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val timeLabel = formatRemaining(remainingMs)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus session active")
            .setContentText(timeLabel)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pending)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun formatRemaining(remainingMs: Long): String {
        val totalSec = (remainingMs.coerceAtLeast(0L)) / 1000L
        val m = totalSec / 60L
        val s = totalSec % 60L
        return String.format(Locale.US, "%02d:%02d", m, s)
    }

    companion object {
        private const val TAG = "WaneSessionService"
        const val ACTION_START: String = "com.wane.app.service.WaneSessionService.START"
        const val ACTION_STOP: String = "com.wane.app.service.WaneSessionService.STOP"

        private const val CHANNEL_ID = "wane_session"
        private const val CHANNEL_NAME = "Focus Session"
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_UPDATE_INTERVAL_MS = 1000L
    }
}
