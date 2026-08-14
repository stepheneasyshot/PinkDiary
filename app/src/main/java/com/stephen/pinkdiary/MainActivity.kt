package com.stephen.pinkdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stephen.pinkdiary.ui.navigation.PinkdiaryNavHost
import com.stephen.pinkdiary.ui.onboarding.OnboardingScreen
import com.stephen.pinkdiary.ui.theme.PinkdiaryTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PinkdiaryApp
        setContent {
            PinkdiaryTheme {
                val onboardingDone by app.userSettingsRepository.onboardingCompleted
                    .collectAsStateWithLifecycle(initialValue = null as Boolean?)
                val scope = rememberCoroutineScope()

                when {
                    onboardingDone == false -> OnboardingScreen(
                        onFinished = {
                            scope.launch { app.userSettingsRepository.setOnboardingCompleted() }
                        }
                    )
                    onboardingDone == true -> PinkdiaryNavHost(app)
                    else -> Unit // 读取中，短暂空白
                }
            }
        }
    }
}
