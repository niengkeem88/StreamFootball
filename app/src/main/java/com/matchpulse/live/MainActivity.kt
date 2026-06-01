package com.matchpulse.live

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matchpulse.live.core.ads.AdMobManager
import com.matchpulse.live.core.ads.InterstitialAdManager
import com.matchpulse.live.core.design.theme.MatchPulseTheme
import com.matchpulse.live.feature.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject lateinit var adMobManager: AdMobManager
    @Inject lateinit var interstitialAdManager: InterstitialAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        adMobManager.gatherConsentAndInitialize(this)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            var showOnboarding by remember { mutableStateOf(true) }

            MatchPulseTheme(darkTheme = settings.darkMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when {
                        showOnboarding -> OnboardingFlow(
                            viewModel = viewModel,
                            adMobManager = adMobManager,
                            interstitialAdManager = interstitialAdManager,
                            onComplete = { showOnboarding = false }
                        )
                        else -> MainApp(viewModel, adMobManager, interstitialAdManager)
                    }
                }
            }
        }
    }
}
