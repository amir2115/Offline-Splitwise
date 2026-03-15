package com.encer.offlinesplitwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.encer.offlinesplitwise.data.AppContainer
import com.encer.offlinesplitwise.ui.OfflineSplitwiseApp
import com.encer.offlinesplitwise.ui.theme.OfflineSplitwiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appContainer = remember { AppContainer(applicationContext) }
            OfflineSplitwiseTheme {
                OfflineSplitwiseApp(appContainer = appContainer)
            }
        }
    }
}
