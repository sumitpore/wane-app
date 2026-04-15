package com.wane.app.data.repository.impl

import com.wane.app.data.db.dao.WaterThemeDao
import com.wane.app.data.db.entity.WaterThemeEntity
import com.wane.app.data.repository.ThemeRepository
import com.wane.app.shared.WaterTheme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val waterThemeDao: WaterThemeDao,
) : ThemeRepository {

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
