package com.stephen.pinkdiary.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stephen.pinkdiary.data.prediction.CyclePrediction
import com.stephen.pinkdiary.ui.home.HomeUiState
import com.stephen.pinkdiary.ui.theme.OnWarningContainer
import com.stephen.pinkdiary.ui.theme.WarningContainer

enum class StatusKind { COLD_START, ON_PERIOD, OVERDUE, DUE_TODAY, UPCOMING }

data class StatusCardModel(
    val kind: StatusKind,
    val title: String,
    val subtitle: String
)

/** 根据预测结果生成状态卡文案（纯函数，可单测）。 */
fun buildStatusCardModel(prediction: CyclePrediction?): StatusCardModel {
    if (prediction == null) {
        return StatusCardModel(
            StatusKind.COLD_START,
            "开始记录你的经期",
            "点击日历中的日期，标记你的经期开始"
        )
    }
    val cycle = "周期第 ${prediction.cycleDay} 天 · 平均周期 ${prediction.averageCycleLength} 天"
    return when {
        prediction.isOnPeriod -> StatusCardModel(
            StatusKind.ON_PERIOD,
            "经期中 · 第 ${prediction.periodDay} 天",
            "$cycle · 预计持续 ${prediction.durationDays} 天"
        )
        prediction.daysUntilNext < 0 -> StatusCardModel(
            StatusKind.OVERDUE,
            "预测已过 ${-prediction.daysUntilNext} 天，请记录",
            "若已来经期，点击日历中的日期更新记录"
        )
        prediction.daysUntilNext == 0 -> StatusCardModel(
            StatusKind.DUE_TODAY,
            "预测今天开始",
            cycle
        )
        else -> StatusCardModel(
            StatusKind.UPCOMING,
            "距下次经期 ${prediction.daysUntilNext} 天",
            cycle
        )
    }
}

@Composable
fun StatusCard(state: HomeUiState, modifier: Modifier = Modifier) {
    val model = buildStatusCardModel(state.prediction)

    val containerColor = when (model.kind) {
        StatusKind.ON_PERIOD -> MaterialTheme.colorScheme.primaryContainer
        StatusKind.OVERDUE -> WarningContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val titleColor = when (model.kind) {
        StatusKind.ON_PERIOD -> MaterialTheme.colorScheme.onPrimaryContainer
        StatusKind.OVERDUE -> OnWarningContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(model.title, style = MaterialTheme.typography.titleLarge, color = titleColor)
            Text(
                text = model.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
