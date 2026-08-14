package com.stephen.pinkdiary.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stephen.pinkdiary.ui.theme.PeriodPink
import com.stephen.pinkdiary.ui.theme.PeriodPinkLight
import java.time.LocalDate

@Composable
fun CalendarDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isPeriodDay: Boolean,
    isPredictedDay: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberColor = when {
        isPeriodDay -> Color.White
        isPredictedDay -> PeriodPink
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }

    val background = when {
        isPeriodDay -> PeriodPink
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    val ringColor = when {
        isPredictedDay && !isPeriodDay -> PeriodPinkLight
        isToday && !isPeriodDay && !isPredictedDay -> MaterialTheme.colorScheme.primary
        else -> null
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(background)
            .then(
                if (ringColor != null) Modifier.border(1.5.dp, ringColor, CircleShape) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = numberColor
        )
        if (isToday) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isPeriodDay) Color.White else MaterialTheme.colorScheme.primary)
            )
        }
    }
}
