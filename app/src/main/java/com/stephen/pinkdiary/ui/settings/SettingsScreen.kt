package com.stephen.pinkdiary.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stephen.pinkdiary.R

@Composable
fun SettingsRoute(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowMessage -> snackbarHostState.showSnackbar(
                    context.getString(effect.messageRes)
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        SettingsScreen(state = state, onIntent = viewModel::onIntent)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit
) {
    val settings = state.settings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        StepperRow(
            title = stringResource(R.string.settings_default_cycle_length),
            value = settings.defaultCycleLength,
            min = SettingsViewModel.MIN_CYCLE_LENGTH,
            max = SettingsViewModel.MAX_CYCLE_LENGTH,
            suffix = stringResource(R.string.settings_unit_days),
            enabled = !state.isLoading,
            onValueChange = { onIntent(SettingsIntent.DefaultCycleLengthChanged(it)) }
        )
        StepperRow(
            title = stringResource(R.string.settings_default_period_length),
            value = settings.defaultPeriodLength,
            min = SettingsViewModel.MIN_PERIOD_LENGTH,
            max = SettingsViewModel.MAX_PERIOD_LENGTH,
            suffix = stringResource(R.string.settings_unit_days),
            enabled = !state.isLoading,
            onValueChange = { onIntent(SettingsIntent.DefaultPeriodLengthChanged(it)) }
        )
        StepperRow(
            title = stringResource(R.string.settings_recent_cycles),
            value = settings.recentN,
            min = SettingsViewModel.MIN_RECENT_CYCLES,
            max = SettingsViewModel.MAX_RECENT_CYCLES,
            suffix = stringResource(R.string.settings_unit_count),
            enabled = !state.isLoading,
            onValueChange = { onIntent(SettingsIntent.RecentCyclesChanged(it)) }
        )

        Text(
            text = stringResource(R.string.settings_prediction_rule),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StepperRow(
    title: String,
    value: Int,
    min: Int,
    max: Int,
    suffix: String,
    enabled: Boolean,
    onValueChange: (Int) -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium
        )
        TextButton(onClick = { onValueChange(value - 1) }, enabled = enabled && value > min) {
            Text(stringResource(R.string.stepper_decrement), style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = stringResource(R.string.stepper_value_format, value, suffix),
            modifier = Modifier.widthIn(min = 64.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        TextButton(onClick = { onValueChange(value + 1) }, enabled = enabled && value < max) {
            Text(stringResource(R.string.stepper_increment), style = MaterialTheme.typography.titleLarge)
        }
    }
}
