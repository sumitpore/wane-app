package com.wane.app.data.repository

import com.wane.app.shared.WaterTheme
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {

    fun observeAllThemes(): Flow<List<WaterTheme>>

    fun observePurchasedThemes(): Flow<List<WaterTheme>>

    suspend fun getThemeById(id: String): WaterTheme?

    suspend fun markThemePurchased(themeId: String, purchaseToken: String)

    suspend fun seedDefaultThemes()
}
