package com.wane.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.wane.app.data.repository.PreferencesRepository
import com.wane.app.data.repository.ThemeRepository
import com.wane.app.shared.WaneRoute
import com.wane.app.ui.navigation.WaneNavHost
import com.wane.app.ui.theme.WaneTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            themeRepository.seedDefaultThemes()
        }

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
