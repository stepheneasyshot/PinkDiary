package com.stephen.pinkdiary.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarMonth(
    month: YearMonth,
    today: LocalDate,
    periodDates: Set<LocalDate>,
    predictedDates: Set<LocalDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDay = month.atDay(1)
    val leadingEmpty = firstDay.dayOfWeek.value - 1 // 周一开头
    val startDate = firstDay.minusDays(leadingEmpty.toLong())
    val days = (0 until 42).map { startDate.plusDays(it.toLong()) }

    Column(modifier = modifier) {
        Row(Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        for (week in 0 until 6) {
            Row(Modifier.fillMaxWidth()) {
                for (i in 0 until 7) {
                    val date = days[week * 7 + i]
                    CalendarDayCell(
                        date = date,
                        isCurrentMonth = YearMonth.from(date) == month,
                        isToday = date == today,
                        isPeriodDay = date in periodDates,
                        isPredictedDay = date in predictedDates,
                        isSelected = date == selectedDate,
                        onClick = { onDateSelected(date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
