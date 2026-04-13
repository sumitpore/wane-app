package com.wane.app.data.repository.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.wane.app.data.datastore.PreferenceKeys
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.shared.AutoLockConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferencesRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val stringListSerializer = ListSerializer(String.serializer())

    override fun observeDefaultDuration(): Flow<Int> =
        dataStore.data.map { prefs ->
            prefs[PreferenceKeys.DEFAULT_DURATION_MINUTES] ?: DEFAULT_DURATION_MINUTES
        }.catch { emit(DEFAULT_DURATION_MINUTES) }

    override fun observeAutoLockConfig(): Flow<AutoLockConfig> =
        dataStore.data.map { prefs -> readAutoLockConfig(prefs) }
            .catch { emit(AutoLockConfig()) }

    override fun observeSelectedThemeId(): Flow<String> =
        dataStore.data.map { prefs ->
            prefs[PreferenceKeys.SELECTED_THEME_ID] ?: DEFAULT_THEME_ID
        }.catch { emit(DEFAULT_THEME_ID) }

    override fun observeEmergencyContacts(): Flow<List<String>> =
        dataStore.data.map { prefs -> readEmergencyContacts(prefs) }
            .catch { emit(emptyList()) }

    override fun observeAmbientSoundsEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[PreferenceKeys.AMBIENT_SOUNDS_ENABLED] ?: DEFAULT_AMBIENT_SOUNDS
        }.catch { emit(DEFAULT_AMBIENT_SOUNDS) }

    override fun observeHapticFeedbackEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[PreferenceKeys.HAPTIC_FEEDBACK_ENABLED] ?: DEFAULT_HAPTIC_FEEDBACK
        }.catch { emit(DEFAULT_HAPTIC_FEEDBACK) }

    override fun observeOnboardingCompleted(): Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[PreferenceKeys.ONBOARDING_COMPLETED] ?: DEFAULT_ONBOARDING_COMPLETED
        }.catch { emit(DEFAULT_ONBOARDING_COMPLETED) }

    override suspend fun setDefaultDuration(minutes: Int) {
        if (minutes !in DURATION_RANGE) return
        try {
            dataStore.edit { it[PreferenceKeys.DEFAULT_DURATION_MINUTES] = minutes }
        } catch (_: Exception) {
            // no-op
        }
    }

    override suspend fun setAutoLockConfig(config: AutoLockConfig) {
        if (!isValidAutoLockConfig(config)) return
        try {
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.AUTO_LOCK_ENABLED] = config.enabled
                prefs[PreferenceKeys.AUTO_LOCK_DURATION_MINUTES] = config.durationMinutes
                prefs[PreferenceKeys.AUTO_LOCK_GRACE_PERIOD_SECONDS] = config.gracePeriodSeconds
                prefs[PreferenceKeys.AUTO_LOCK_SKIP_WHILE_CHARGING] = config.skipWhileCharging
                if (config.skipStartHour == null) {
                    prefs[PreferenceKeys.AUTO_LOCK_SKIP_START_HOUR] = SKIP_SENTINEL
                    prefs[PreferenceKeys.AUTO_LOCK_SKIP_START_MINUTE] = SKIP_SENTINEL
                    prefs[PreferenceKeys.AUTO_LOCK_SKIP_END_HOUR] = SKIP_SENTINEL
                    prefs[PreferenceKeys.AUTO_LOCK_SKIP_END_MINUTE] = SKIP_SENTINEL
                } else {
                    prefs[PreferenceKeys.AUTO_LOCK_SKIP_START_HOUR] = config.skipStartHour
                    prefs[PreferenceKeys.AUTO_LOCK_SKIP_START_MINUTE] = config.skipStartMinute!!
                    prefs[PreferenceKeys.AUTO_LOCK_SKIP_END_HOUR] = config.skipEndHour!!
                    prefs[PreferenceKeys.AUTO_LOCK_SKIP_END_MINUTE] = config.skipEndMinute!!
                }
            }
        } catch (_: Exception) {
            // no-op
        }
    }

    override suspend fun setSelectedThemeId(themeId: String) {
        if (themeId.isBlank()) return
        try {
            dataStore.edit { it[PreferenceKeys.SELECTED_THEME_ID] = themeId.trim() }
        } catch (_: Exception) {
            // no-op
        }
    }

    override suspend fun setEmergencyContacts(contacts: List<String>) {
        val cleaned = contacts.map { it.trim() }.filter { it.isNotEmpty() }
        try {
            val encoded = json.encodeToString(stringListSerializer, cleaned)
            dataStore.edit { it[PreferenceKeys.EMERGENCY_CONTACTS_JSON] = encoded }
        } catch (_: Exception) {
            // no-op
        }
    }

    override suspend fun setAmbientSoundsEnabled(enabled: Boolean) {
        try {
            dataStore.edit { it[PreferenceKeys.AMBIENT_SOUNDS_ENABLED] = enabled }
        } catch (_: Exception) {
            // no-op
        }
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        try {
            dataStore.edit { it[PreferenceKeys.HAPTIC_FEEDBACK_ENABLED] = enabled }
        } catch (_: Exception) {
            // no-op
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        try {
            dataStore.edit { it[PreferenceKeys.ONBOARDING_COMPLETED] = completed }
        } catch (_: Exception) {
            // no-op
        }
    }

    private fun readAutoLockConfig(prefs: Preferences): AutoLockConfig {
        val sh = prefs[PreferenceKeys.AUTO_LOCK_SKIP_START_HOUR] ?: SKIP_SENTINEL
        val sm = prefs[PreferenceKeys.AUTO_LOCK_SKIP_START_MINUTE] ?: SKIP_SENTINEL
        val eh = prefs[PreferenceKeys.AUTO_LOCK_SKIP_END_HOUR] ?: SKIP_SENTINEL
        val em = prefs[PreferenceKeys.AUTO_LOCK_SKIP_END_MINUTE] ?: SKIP_SENTINEL
        val (skipH, skipSm, skipEh, skipEm) = when {
            sh == SKIP_SENTINEL && sm == SKIP_SENTINEL && eh == SKIP_SENTINEL && em == SKIP_SENTINEL ->
                Quad(null, null, null, null)
            sh != SKIP_SENTINEL && sm != SKIP_SENTINEL && eh != SKIP_SENTINEL && em != SKIP_SENTINEL ->
                Quad(sh, sm, eh, em)
            else ->
                Quad(null, null, null, null)
        }
        return AutoLockConfig(
            enabled = prefs[PreferenceKeys.AUTO_LOCK_ENABLED] ?: false,
            durationMinutes = prefs[PreferenceKeys.AUTO_LOCK_DURATION_MINUTES]
                ?: DEFAULT_AUTO_LOCK_DURATION_MINUTES,
            gracePeriodSeconds = prefs[PreferenceKeys.AUTO_LOCK_GRACE_PERIOD_SECONDS]
                ?: DEFAULT_GRACE_PERIOD_SECONDS,
            skipStartHour = skipH,
            skipStartMinute = skipSm,
            skipEndHour = skipEh,
            skipEndMinute = skipEm,
            skipWhileCharging = prefs[PreferenceKeys.AUTO_LOCK_SKIP_WHILE_CHARGING] ?: false,
        )
    }

    private data class Quad(
        val skipH: Int?,
        val skipSm: Int?,
        val skipEh: Int?,
        val skipEm: Int?,
    )

    private fun readEmergencyContacts(prefs: Preferences): List<String> {
        val raw = prefs[PreferenceKeys.EMERGENCY_CONTACTS_JSON] ?: return emptyList()
        return try {
            json.decodeFromString(stringListSerializer, raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun isValidAutoLockConfig(config: AutoLockConfig): Boolean {
        if (config.durationMinutes !in DURATION_RANGE) return false
        if (config.gracePeriodSeconds !in GRACE_RANGE) return false
        val fields = listOf(
            config.skipStartHour,
            config.skipStartMinute,
            config.skipEndHour,
            config.skipEndMinute,
        )
        val allNull = fields.all { it == null }
        val anyNull = fields.any { it == null }
        if (anyNull && !allNull) return false
        if (!allNull) {
            val sh = config.skipStartHour!!
            val sm = config.skipStartMinute!!
            val eh = config.skipEndHour!!
            val em = config.skipEndMinute!!
            if (sh !in HOUR_RANGE || sm !in MINUTE_RANGE || eh !in HOUR_RANGE || em !in MINUTE_RANGE) {
                return false
            }
            if (sh == eh && sm == em) return false
        }
        return true
    }

    companion object {
        const val DEFAULT_DURATION_MINUTES = 25
        const val DEFAULT_THEME_ID = "default"
        const val DEFAULT_AUTO_LOCK_DURATION_MINUTES = 30
        const val DEFAULT_GRACE_PERIOD_SECONDS = 10
        const val SKIP_SENTINEL = -1

        private val DURATION_RANGE = 5..120
        private val GRACE_RANGE = 5..60
        private val HOUR_RANGE = 0..23
        private val MINUTE_RANGE = 0..59

        private const val DEFAULT_AMBIENT_SOUNDS = false
        private const val DEFAULT_HAPTIC_FEEDBACK = true
        private const val DEFAULT_ONBOARDING_COMPLETED = false
    }
}
