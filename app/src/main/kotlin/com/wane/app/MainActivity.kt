package com.wane.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.wane.app.ui.theme.BackgroundDeep
import com.wane.app.ui.theme.WaneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WaneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDeep,
                ) {
                    // Nav3 NavHost will be wired here in Phase 4
                }
            }
        }
    }
}
