package com.wane.app.data.repository.impl

import com.wane.app.data.db.dao.WaterThemeDao
import com.wane.app.data.db.entity.WaterThemeEntity
import com.wane.app.data.repository.ThemeRepository
import com.wane.app.shared.WaterTheme
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val waterThemeDao: WaterThemeDao,
) : ThemeRepository {

    override fun observeAllThemes(): Flow<List<WaterTheme>> =
        waterThemeDao.getAllThemes()
            .map { list -> list.map(WaterThemeEntity::toShared) }
            .catch { emit(emptyList()) }

    override fun observePurchasedThemes(): Flow<List<WaterTheme>> =
        waterThemeDao.getPurchasedThemes()
            .map { list -> list.map(WaterThemeEntity::toShared) }
            .catch { emit(emptyList()) }

    override suspend fun getThemeById(id: String): WaterTheme? = try {
        waterThemeDao.getThemeById(id)?.toShared()
    } catch (_: Exception) {
        null
    }

    override suspend fun markThemePurchased(themeId: String, purchaseToken: String) {
        try {
            val now = System.currentTimeMillis()
            waterThemeDao.updatePurchaseStatus(
                themeId = themeId,
                purchaseToken = purchaseToken,
                purchaseTimestamp = now,
            )
        } catch (_: Exception) {
            // no-op
        }
    }

    override suspend fun seedDefaultThemes() {
        try {
            waterThemeDao.upsert(
                WaterThemeEntity.fromShared(
                    WaterTheme(
                        id = "default",
                        displayName = "Still Water",
                        isPurchased = true,
                        purchaseToken = null,
                        purchaseTimestamp = null,
                    ),
                ),
            )
        } catch (_: Exception) {
            // no-op
        }
    }
}
