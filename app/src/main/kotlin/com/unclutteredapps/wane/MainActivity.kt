package com.unclutteredapps.wane

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unclutteredapps.wane.data.repository.PreferencesRepository
import com.unclutteredapps.wane.shared.WaneRoute
import com.unclutteredapps.wane.ui.navigation.WaneNavHost
import com.unclutteredapps.wane.ui.theme.WaneTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        setContent {
            WaneTheme {
                val onboardingCompleted by preferencesRepository
                    .observeOnboardingCompleted()
                    .collectAsStateWithLifecycle(initialValue = null)

                when (onboardingCompleted) {
                    null -> Unit
                    false -> WaneNavHost(startRoute = WaneRoute.Onboarding)
                    true -> WaneNavHost(startRoute = WaneRoute.Home)
                }
            }
        }
    }
}
