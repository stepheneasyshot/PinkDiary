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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.data.prediction.CyclePrediction
import com.stephen.pinkdiary.ui.home.HomeUiState
import com.stephen.pinkdiary.ui.theme.OnWarningContainer
import com.stephen.pinkdiary.ui.theme.WarningContainer

enum class StatusKind { COLD_START, ON_PERIOD, OVERDUE, DUE_TODAY, UPCOMING }

/** 根据预测结果判定状态卡类型（纯函数，可单测）。 */
fun statusKind(prediction: CyclePrediction?): StatusKind = when {
    prediction == null -> StatusKind.COLD_START
    prediction.isOnPeriod -> StatusKind.ON_PERIOD
    prediction.daysUntilNext < 0 -> StatusKind.OVERDUE
    prediction.daysUntilNext == 0 -> StatusKind.DUE_TODAY
    else -> StatusKind.UPCOMING
}

@Composable
fun StatusCard(state: HomeUiState, modifier: Modifier = Modifier) {
    val prediction = state.prediction
    val kind = statusKind(prediction)

    val title: String
    val subtitle: String
    when {
        prediction == null -> {
            title = stringResource(R.string.status_cold_start_title)
            subtitle = stringResource(R.string.guide_mark_period_start)
        }
        prediction.isOnPeriod -> {
            title = stringResource(R.string.status_on_period, prediction.periodDay ?: 1)
            subtitle = stringResource(
                R.string.status_on_period_subtitle,
                prediction.cycleDay,
                prediction.averageCycleLength,
                prediction.durationDays
            )
        }
        prediction.daysUntilNext < 0 -> {
            title = stringResource(R.string.status_overdue, -prediction.daysUntilNext)
            subtitle = stringResource(R.string.status_overdue_subtitle)
        }
        prediction.daysUntilNext == 0 -> {
            title = stringResource(R.string.status_due_today)
            subtitle = stringResource(R.string.status_cycle_summary, prediction.cycleDay, prediction.averageCycleLength)
        }
        else -> {
            title = stringResource(R.string.status_upcoming, prediction.daysUntilNext)
            subtitle = stringResource(R.string.status_cycle_summary, prediction.cycleDay, prediction.averageCycleLength)
        }
    }

    val containerColor = when (kind) {
        StatusKind.ON_PERIOD -> MaterialTheme.colorScheme.primaryContainer
        StatusKind.OVERDUE -> WarningContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val titleColor = when (kind) {
        StatusKind.ON_PERIOD -> MaterialTheme.colorScheme.onPrimaryContainer
        StatusKind.OVERDUE -> OnWarningContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = titleColor)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
