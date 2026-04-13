package com.wane.app.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val DEFAULT_DURATION_MINUTES = intPreferencesKey("default_duration_minutes")
    val AUTO_LOCK_ENABLED = booleanPreferencesKey("auto_lock_enabled")
    val AUTO_LOCK_DURATION_MINUTES = intPreferencesKey("auto_lock_duration_minutes")
    val AUTO_LOCK_GRACE_PERIOD_SECONDS = intPreferencesKey("auto_lock_grace_period_seconds")
    val AUTO_LOCK_SKIP_START_HOUR = intPreferencesKey("auto_lock_skip_start_hour")
    val AUTO_LOCK_SKIP_START_MINUTE = intPreferencesKey("auto_lock_skip_start_minute")
    val AUTO_LOCK_SKIP_END_HOUR = intPreferencesKey("auto_lock_skip_end_hour")
    val AUTO_LOCK_SKIP_END_MINUTE = intPreferencesKey("auto_lock_skip_end_minute")
    val AUTO_LOCK_SKIP_WHILE_CHARGING = booleanPreferencesKey("auto_lock_skip_while_charging")
    val SELECTED_THEME_ID = stringPreferencesKey("selected_theme_id")
    val EMERGENCY_CONTACTS_JSON = stringPreferencesKey("emergency_contacts_json")
    val AMBIENT_SOUNDS_ENABLED = booleanPreferencesKey("ambient_sounds_enabled")
    val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
}
