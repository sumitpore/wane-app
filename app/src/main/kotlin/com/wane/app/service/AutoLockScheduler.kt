package com.wane.app.service

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import com.wane.app.MainActivity
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.shared.AutoLockConfig
import com.wane.app.shared.SessionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoLockScheduler
    @Inject
    constructor(
        private val preferencesRepository: PreferencesRepository,
        private val sessionManager: SessionManager,
        @ApplicationContext context: Context,
        @ApplicationScope private val scope: CoroutineScope,
    ) {
        private val app = context.applicationContext as Application

        @Volatile
        private var latestConfig: AutoLockConfig = AutoLockConfig()

        @Volatile
        private var graceActive: Boolean = false

        private var graceJob: Job? = null

        private val lock = Any()

        private val lifecycleCallbacks =
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) {}

                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityResumed(activity: Activity) {
                    try {
                        if (activity !is MainActivity) return
                        synchronized(lock) {
                            if (!graceActive) return
                            graceJob?.cancel()
                            graceJob = null
                            graceActive = false
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "onActivityResumed", e)
                    }
                }

                override fun onActivityPaused(activity: Activity) {}

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) {}

                override fun onActivityDestroyed(activity: Activity) {}
            }

        init {
            app.registerActivityLifecycleCallbacks(lifecycleCallbacks)
            scope.launch {
                try {
                    preferencesRepository
                        .observeAutoLockConfig()
                        .collect { cfg -> latestConfig = cfg }
                } catch (e: Exception) {
                    Log.e(TAG, "Preference collection failed", e)
                }
            }
        }

        fun onScreenUnlocked() {
            try {
                val config = latestConfig
                if (!config.enabled) return
                if (sessionManager.sessionState.value !is SessionState.Idle) return
                if (isInSkipWindow(config)) return
                if (config.skipWhileCharging && isDeviceCharging()) return

                synchronized(lock) {
                    graceJob?.cancel()
                    graceActive = true
                    val snapshotConfig = latestConfig
                    graceJob =
                        scope.launch {
                            try {
                                delay(snapshotConfig.gracePeriodSeconds * 1000L)
                                if (!isActive) return@launch
                                synchronized(lock) {
                                    graceActive = false
                                }
                                val cfg = latestConfig
                                if (!cfg.enabled) return@launch
                                if (sessionManager.sessionState.value !is SessionState.Idle) return@launch
                                if (isInSkipWindow(cfg)) return@launch
                                if (cfg.skipWhileCharging && isDeviceCharging()) return@launch
                                openApp()
                            } catch (e: Exception) {
                                Log.e(TAG, "Grace period completion failed", e)
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "onScreenUnlocked failed", e)
            }
        }

        fun onScreenLocked() {
            try {
                synchronized(lock) {
                    graceJob?.cancel()
                    graceJob = null
                    graceActive = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "onScreenLocked failed", e)
            }
        }

        private fun openApp() {
            try {
                val intent =
                    Intent(app, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                app.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "openApp failed", e)
            }
        }

        private fun isDeviceCharging(): Boolean =
            try {
                val bm = app.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                bm.isCharging
            } catch (e: Exception) {
                Log.e(TAG, "isDeviceCharging", e)
                false
            }

        private fun isInSkipWindow(config: AutoLockConfig): Boolean {
            val sh = config.skipStartHour ?: return false
            val sm = config.skipStartMinute ?: return false
            val eh = config.skipEndHour ?: return false
            val em = config.skipEndMinute ?: return false

            val cal = java.util.Calendar.getInstance()
            val nowMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
            val startMinutes = sh * 60 + sm
            val endMinutes = eh * 60 + em
            return if (startMinutes <= endMinutes) {
                nowMinutes in startMinutes..endMinutes
            } else {
                nowMinutes >= startMinutes || nowMinutes <= endMinutes
            }
        }

        companion object {
            private const val TAG = "AutoLockScheduler"
        }
    }
