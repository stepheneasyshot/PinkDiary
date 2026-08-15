package com.stephen.pinkdiary.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.data.local.PeriodRecord
import com.stephen.pinkdiary.data.prediction.CyclePhase

/**
 * 选中日期后显示在日历下方的内联记录操作区。
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

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(
                    when {
                        isOngoing -> R.string.record_ongoing
                        coveringRecord != null -> R.string.record_recorded
                        isPredictedPeriod -> R.string.record_predicted_period
                        cyclePhase == CyclePhase.FOLLICULAR -> R.string.record_phase_follicular
                        cyclePhase == CyclePhase.OVULATION -> R.string.record_phase_ovulation
                        cyclePhase == CyclePhase.LUTEAL -> R.string.record_phase_luteal
                        isFutureDate -> R.string.record_future_disabled
                        else -> R.string.record_empty
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            when {
                isOngoing -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onMarkEnd, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.record_mark_end))
                    }
                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.record_delete))
                    }
                }

                coveringRecord != null -> OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.record_delete))
                }

                else -> Button(
                    onClick = onMarkStart,
                    enabled = !isFutureDate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.record_mark_start))
                }
            }
        }
    }
}
