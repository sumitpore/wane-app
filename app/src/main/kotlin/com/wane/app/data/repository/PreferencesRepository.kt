package com.wane.app.data.repository

import com.wane.app.shared.AutoLockConfig
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    // Read operations (Flow-based, reactive)

    fun observeDefaultDuration(): Flow<Int>

    fun observeAutoLockConfig(): Flow<AutoLockConfig>

    fun observeSelectedThemeId(): Flow<String>

    fun observeEmergencyContacts(): Flow<List<String>>

    fun observeAmbientSoundsEnabled(): Flow<Boolean>

    fun observeHapticFeedbackEnabled(): Flow<Boolean>

    fun observeOnboardingCompleted(): Flow<Boolean>

    // Write operations (suspend, atomic)

    suspend fun setDefaultDuration(minutes: Int)

    suspend fun setAutoLockConfig(config: AutoLockConfig)

    suspend fun setSelectedThemeId(themeId: String)

    suspend fun setEmergencyContacts(contacts: List<String>)

    suspend fun setAmbientSoundsEnabled(enabled: Boolean)

    suspend fun setHapticFeedbackEnabled(enabled: Boolean)

    suspend fun setOnboardingCompleted(completed: Boolean)
}
