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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        StepperRow(
            title = "默认周期长度",
            value = settings.defaultCycleLength,
            min = 21,
            max = 45,
            suffix = "天",
            onValueChange = { viewModel.update(settings.copy(defaultCycleLength = it)) }
        )
        StepperRow(
            title = "默认经期长度",
            value = settings.defaultPeriodLength,
            min = 2,
            max = 10,
            suffix = "天",
            onValueChange = { viewModel.update(settings.copy(defaultPeriodLength = it)) }
        )
        StepperRow(
            title = "最近周期数",
            value = settings.recentN,
            min = 1,
            max = 12,
            suffix = "个",
            onValueChange = { viewModel.update(settings.copy(recentN = it)) }
        )

        Text(
            text = "预测规则：下次经期开始 = 最近一次开始日 + 平均周期；经期长度取已结束记录的平均值。尚无历史记录时，使用上面的默认值进行估算。",
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
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = "$value $suffix",
            modifier = Modifier.widthIn(min = 64.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        TextButton(onClick = { onValueChange(value + 1) }, enabled = value < max) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}
