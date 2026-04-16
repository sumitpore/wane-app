package com.wane.app.service.di

import com.wane.app.service.AppBlocker
import com.wane.app.service.ApplicationScope
import com.wane.app.service.AutoLockScheduler
import com.wane.app.service.RepeatedCallerTracker
import com.wane.app.service.SessionManager
import com.wane.app.service.SessionManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ScreenLockServiceEntryPoint {
    fun autoLockScheduler(): AutoLockScheduler
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AccessibilityServiceEntryPoint {
    fun appBlocker(): AppBlocker
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationListenerEntryPoint {
    fun sessionManager(): SessionManager

    fun repeatedCallerTracker(): RepeatedCallerTracker

    fun appBlocker(): AppBlocker
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceBindingModule {
    @Binds
    @Singleton
    abstract fun bindSessionManager(impl: SessionManagerImpl): SessionManager
}

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
