package com.unclutteredapps.wane.data.repository

import com.unclutteredapps.wane.shared.AutoLockConfig
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    // Read operations (Flow-based, reactive)

    fun observeDefaultDuration(): Flow<Int>

    fun observeAutoLockConfig(): Flow<AutoLockConfig>

    fun observeOnboardingCompleted(): Flow<Boolean>

    // Write operations (suspend, atomic)

    suspend fun setDefaultDuration(minutes: Int)

    suspend fun setAutoLockConfig(config: AutoLockConfig)

    suspend fun setOnboardingCompleted(completed: Boolean)
}
