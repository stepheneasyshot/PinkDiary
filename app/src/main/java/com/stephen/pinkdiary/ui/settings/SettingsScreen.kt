package com.stephen.pinkdiary.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stephen.pinkdiary.R

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

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
            min = 21,
            max = 45,
            suffix = stringResource(R.string.settings_unit_days),
            onValueChange = { viewModel.update(settings.copy(defaultCycleLength = it)) }
        )
        StepperRow(
            title = stringResource(R.string.settings_default_period_length),
            value = settings.defaultPeriodLength,
            min = 2,
            max = 10,
            suffix = stringResource(R.string.settings_unit_days),
            onValueChange = { viewModel.update(settings.copy(defaultPeriodLength = it)) }
        )
        StepperRow(
            title = stringResource(R.string.settings_recent_cycles),
            value = settings.recentN,
            min = 1,
            max = 12,
            suffix = stringResource(R.string.settings_unit_count),
            onValueChange = { viewModel.update(settings.copy(recentN = it)) }
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
    onValueChange: (Int) -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium
        )
        TextButton(onClick = { onValueChange(value - 1) }, enabled = value > min) {
            Text(stringResource(R.string.stepper_decrement), style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = stringResource(R.string.stepper_value_format, value, suffix),
            modifier = Modifier.widthIn(min = 64.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        TextButton(onClick = { onValueChange(value + 1) }, enabled = value < max) {
            Text(stringResource(R.string.stepper_increment), style = MaterialTheme.typography.titleLarge)
        }
    }
}
