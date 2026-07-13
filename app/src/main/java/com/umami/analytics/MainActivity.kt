package com.umami.analytics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            UmamiTheme {
                AppNavigation(
                    apiService = app.apiService,
                    database = app.database,
                    sessionManager = app.sessionManager,
                    networkObserver = app.networkObserver
                )
            }
        }
    }
}
