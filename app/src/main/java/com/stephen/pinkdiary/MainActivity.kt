package com.stephen.pinkdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stephen.pinkdiary.ui.app.AppDestination
import com.stephen.pinkdiary.ui.app.AppEffect
import com.stephen.pinkdiary.ui.app.AppIntent
import com.stephen.pinkdiary.ui.app.AppViewModel
import com.stephen.pinkdiary.ui.navigation.PinkdiaryNavHost
import com.stephen.pinkdiary.ui.onboarding.OnboardingScreen
import com.stephen.pinkdiary.ui.theme.PinkdiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PinkdiaryApp
        setContent {
            PinkdiaryTheme {
                val viewModel: AppViewModel = viewModel(factory = AppViewModel.factory(app))
                PinkdiaryAppRoot(app = app, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun PinkdiaryAppRoot(app: PinkdiaryApp, viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AppEffect.ShowMessage -> snackbarHostState.showSnackbar(
                    message = context.getString(effect.messageRes)
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        when (state.destination) {
            AppDestination.ONBOARDING -> OnboardingScreen(
                onFinished = { viewModel.onIntent(AppIntent.OnboardingFinished) }
            )
            AppDestination.MAIN -> PinkdiaryNavHost(app)
            null -> Unit
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
