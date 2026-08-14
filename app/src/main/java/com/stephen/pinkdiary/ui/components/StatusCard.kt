package com.stephen.pinkdiary.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stephen.pinkdiary.ui.home.HomeUiState

@Composable
fun StatusCard(state: HomeUiState, modifier: Modifier = Modifier) {
    val prediction = state.prediction

    val title = when {
        prediction == null -> "记录你的经期"
        prediction.isOnPeriod -> "经期中 · 第 ${prediction.periodDay} 天"
        prediction.daysUntilNext > 0 -> "距下次经期 ${prediction.daysUntilNext} 天"
        prediction.daysUntilNext == 0 -> "预测今天开始"
        else -> "预测已过 ${-prediction.daysUntilNext} 天，请记录"
    }

    val subtitle = if (prediction != null) {
        "周期第 ${prediction.cycleDay} 天 · 平均周期 ${prediction.averageCycleLength} 天"
    } else {
        "点击日历中的日期，标记你的经期开始日"
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
