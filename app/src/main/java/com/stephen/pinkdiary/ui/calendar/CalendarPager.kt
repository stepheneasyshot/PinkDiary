package com.stephen.pinkdiary.ui.calendar

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.stephen.pinkdiary.data.prediction.CyclePhaseDates
import java.time.LocalDate
import java.time.YearMonth

/**
 * 支持左右滑动翻页的日历容器。
 * [pagerState] 由外部持有（便于箭头按钮联动），
 * 第 [basePage] 页对应 [initialMonth]，向左翻到上月、向右翻到下月。
 */
@Composable
fun CalendarPager(
    initialMonth: YearMonth,
    pagerState: PagerState,
    basePage: Int,
    today: LocalDate,
    solidPeriodDates: Set<LocalDate>,
    softPeriodDates: Set<LocalDate>,
    predictedDates: Set<LocalDate>,
    cyclePhaseDates: CyclePhaseDates,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        // 单元格宽 = 可用宽 / 7；日历高度 = 表头 + 6 周
        val cellSize = maxWidth / 7
        val calendarHeight = cellSize * 6

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(calendarHeight)
        ) { page ->
            val month = initialMonth.plusMonths((page - basePage).toLong())
            CalendarMonth(
                month = month,
                today = today,
                solidPeriodDates = solidPeriodDates,
                softPeriodDates = softPeriodDates,
                predictedDates = predictedDates,
                cyclePhaseDates = cyclePhaseDates,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
        }
    }
}
