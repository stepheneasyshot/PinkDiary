package com.stephen.pinkdiary.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stephen.pinkdiary.data.prediction.CyclePhase
import java.time.LocalDate

private val MarkCornerRadius = 14.dp
private val DateMarkerSize = 28.dp
private val SelectionRingSize = 36.dp

@Composable
fun CalendarDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    solidPeriodDates: Set<LocalDate>,
    softPeriodDates: Set<LocalDate>,
    predictedDates: Set<LocalDate>,
    cyclePhase: CyclePhase?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = calendarColors()
    val isPeriodDay = date in solidPeriodDates
    val isSoftPeriodDay = date in softPeriodDates
    val isPredictedDay = date in predictedDates
    val phaseColor = when (cyclePhase) {
        CyclePhase.FOLLICULAR -> colors.follicular
        CyclePhase.OVULATION -> colors.ovulation
        CyclePhase.LUTEAL -> colors.luteal
        null -> null
    }

    val markerFill = when {
        isPeriodDay -> colors.period
        isSoftPeriodDay -> colors.softPeriod
        isPredictedDay -> colors.predictedPeriod
        else -> phaseColor
    }

    val numberColor = when {
        isPeriodDay -> colors.onPeriod
        isSoftPeriodDay -> colors.onSoftPeriod
        isPredictedDay -> colors.onPredicted
        isCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        if (markerFill != null) {
            Box(
                Modifier
                    .size(DateMarkerSize)
                    .clip(CircleShape)
                    .background(markerFill)
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(MarkCornerRadius))
                .clickable(onClick = onClick)
        )
        if (isSelected) {
            Box(
                Modifier
                    .size(SelectionRingSize)
                    .border(2.dp, colors.selection, CircleShape)
            )
        }
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = numberColor
        )
        if (isToday) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 1.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isPeriodDay -> colors.onPeriod
                            else -> colors.selection
                        }
                    )
            )
        }
    }
}
