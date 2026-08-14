package com.stephen.pinkdiary.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarMonth(
    month: YearMonth,
    today: LocalDate,
    solidPeriodDates: Set<LocalDate>,
    softPeriodDates: Set<LocalDate>,
    predictedDates: Set<LocalDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDay = month.atDay(1)
    val leadingEmpty = firstDay.dayOfWeek.value - 1 // 周一开头
    val startDate = firstDay.minusDays(leadingEmpty.toLong())
    val days = (0 until 42).map { startDate.plusDays(it.toLong()) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(macaronColorFor(month))
    ) {
        for (week in 0 until 6) {
            Row(Modifier.fillMaxWidth()) {
                for (i in 0 until 7) {
                    val date = days[week * 7 + i]
                    CalendarDayCell(
                        date = date,
                        isCurrentMonth = YearMonth.from(date) == month,
                        isToday = date == today,
                        solidPeriodDates = solidPeriodDates,
                        softPeriodDates = softPeriodDates,
                        predictedDates = predictedDates,
                        isSelected = date == selectedDate,
                        onClick = { onDateSelected(date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 星期表头，固定于翻页容器之外，不随手势翻动。
 */
@Composable
fun WeekdayHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
