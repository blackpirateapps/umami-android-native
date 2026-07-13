package com.umami.analytics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.umami.analytics.ui.navigation.AppNavigation
import com.umami.analytics.ui.theme.UmamiTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as UmamiApplication

        setContent {
            var themeMode by remember { mutableStateOf(app.sessionManager.getThemeMode()) }

            UmamiTheme(themeMode = themeMode) {
                AppNavigation(
                    apiService = app.apiService,
                    database = app.database,
                    sessionManager = app.sessionManager,
                    networkObserver = app.networkObserver,
                    onThemeModeChange = { newMode ->
                        themeMode = newMode
                    }
                )
            }
        }
    }
}
