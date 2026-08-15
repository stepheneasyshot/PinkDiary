package com.stephen.pinkdiary.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.data.local.PeriodRecord
import com.stephen.pinkdiary.data.prediction.CyclePhase
import com.stephen.pinkdiary.ui.calendar.calendarColors

/**
 * 选中日期后显示在日历下方的紧凑状态与操作栏。
 */
@Composable
fun RecordActions(
    coveringRecord: PeriodRecord?,
    cyclePhase: CyclePhase?,
    isPredictedPeriod: Boolean,
    isFutureDate: Boolean,
    onMarkStart: () -> Unit,
    onMarkEnd: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOngoing = coveringRecord != null && coveringRecord.endDateEpochDay == null
    val calendarColors = calendarColors()
    val statusColor = when {
        coveringRecord != null -> calendarColors.period
        isPredictedPeriod -> calendarColors.predictedPeriod
        cyclePhase == CyclePhase.FOLLICULAR -> calendarColors.follicular
        cyclePhase == CyclePhase.OVULATION -> calendarColors.ovulation
        cyclePhase == CyclePhase.LUTEAL -> calendarColors.luteal
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val statusRes = when {
        isOngoing -> R.string.record_ongoing
        coveringRecord != null -> R.string.record_recorded
        isPredictedPeriod -> R.string.record_predicted_period
        cyclePhase == CyclePhase.FOLLICULAR -> R.string.record_phase_follicular
        cyclePhase == CyclePhase.OVULATION -> R.string.record_phase_ovulation
        cyclePhase == CyclePhase.LUTEAL -> R.string.record_phase_luteal
        isFutureDate -> R.string.record_future_date
        else -> R.string.record_empty
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusSummary(
                statusRes = statusRes,
                statusColor = statusColor,
                showFutureHint = isFutureDate,
                modifier = Modifier.weight(1f)
            )

            when {
                isOngoing -> {
                    Button(onClick = onMarkEnd) {
                        Text(stringResource(R.string.record_mark_end))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.record_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                coveringRecord != null -> TextButton(onClick = onDelete) {
                    Text(
                        text = stringResource(R.string.record_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                !isFutureDate -> Button(onClick = onMarkStart) {
                    Text(stringResource(R.string.record_mark_start))
                }
            }
        }
    }
}

@Composable
private fun StatusSummary(
    statusRes: Int,
    statusColor: Color,
    showFutureHint: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(statusRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (showFutureHint) {
                Text(
                    text = stringResource(R.string.record_future_disabled),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
