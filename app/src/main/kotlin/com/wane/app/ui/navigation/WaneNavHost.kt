package com.wane.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.wane.app.shared.WaneRoute
import com.wane.app.ui.home.HomeScreen
import com.wane.app.ui.onboarding.OnboardingScreen
import com.wane.app.ui.session.SessionScreen
import com.wane.app.ui.settings.AutoLockSettingsScreen
import com.wane.app.ui.settings.SettingsScreen

@Composable
fun WaneNavHost(startRoute: WaneRoute) {
    val backStack = remember { mutableStateListOf<WaneRoute>(startRoute) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { route ->
            when (route) {
                WaneRoute.Onboarding -> NavEntry(route) {
                    OnboardingScreen(
                        onOnboardingComplete = dropUnlessResumed {
                            backStack.clear()
                            backStack.add(WaneRoute.Home)
                        },
                    )
                }

                WaneRoute.Home -> NavEntry(route) {
                    HomeScreen(
                        onNavigateToSession = dropUnlessResumed {
                            backStack.add(WaneRoute.Session)
                        },
                        onNavigateToSettings = dropUnlessResumed {
                            backStack.add(WaneRoute.Settings)
                        },
                    )
                }

                WaneRoute.Session -> NavEntry(route) {
                    SessionScreen(
                        onSessionEnd = dropUnlessResumed {
                            backStack.clear()
                            backStack.add(WaneRoute.Home)
                        },
                    )
                }

                WaneRoute.Settings -> NavEntry(route) {
                    SettingsScreen(
                        onNavigateBack = dropUnlessResumed {
                            backStack.removeLastOrNull()
                        },
                        onNavigateToAutoLock = dropUnlessResumed {
                            backStack.add(WaneRoute.AutoLockSettings)
                        },
                    )
                }

                WaneRoute.AutoLockSettings -> NavEntry(route) {
                    AutoLockSettingsScreen(
                        onNavigateBack = dropUnlessResumed {
                            backStack.removeLastOrNull()
                        },
                    )
                }
            }
        },
    )
}
