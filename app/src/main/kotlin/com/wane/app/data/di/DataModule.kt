package com.wane.app.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.wane.app.data.db.WaneDatabase
import com.wane.app.data.db.dao.FocusSessionDao
import com.wane.app.data.db.dao.WaterThemeDao
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.data.repository.SessionRepository
import com.wane.app.data.repository.ThemeRepository
import com.wane.app.data.repository.impl.PreferencesRepositoryImpl
import com.wane.app.data.repository.impl.SessionRepositoryImpl
import com.wane.app.data.repository.impl.ThemeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

private const val WANE_DB_NAME = "wane.db"

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideWaneDatabase(@ApplicationContext context: Context): WaneDatabase {
        return Room.databaseBuilder(
            context,
            WaneDatabase::class.java,
            WANE_DB_NAME,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    @Provides
    fun provideFocusSessionDao(db: WaneDatabase): FocusSessionDao = db.focusSessionDao()

    @Provides
    fun provideWaterThemeDao(db: WaneDatabase): WaterThemeDao = db.waterThemeDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("wane_preferences") },
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
