package com.stephen.pinkdiary.ui.calendar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.stephen.pinkdiary.ui.theme.CalendarSurface
import com.stephen.pinkdiary.ui.theme.CalendarSurfaceDark
import com.stephen.pinkdiary.ui.theme.OnPeriodPink
import com.stephen.pinkdiary.ui.theme.OnPeriodPinkSoft
import com.stephen.pinkdiary.ui.theme.OnPeriodPinkSoftDark
import com.stephen.pinkdiary.ui.theme.OnPredictedPink
import com.stephen.pinkdiary.ui.theme.OnPredictedPinkDark
import com.stephen.pinkdiary.ui.theme.PeriodPink
import com.stephen.pinkdiary.ui.theme.PeriodPinkDark
import com.stephen.pinkdiary.ui.theme.PeriodPinkSoft
import com.stephen.pinkdiary.ui.theme.PeriodPinkSoftDark
import com.stephen.pinkdiary.ui.theme.PredictedPink
import com.stephen.pinkdiary.ui.theme.PredictedPinkDark
import com.stephen.pinkdiary.ui.theme.SelectionPink
import com.stephen.pinkdiary.ui.theme.SelectionPinkDark

@Immutable
internal data class CalendarColors(
    val surface: Color,
    val period: Color,
    val onPeriod: Color,
    val softPeriod: Color,
    val onSoftPeriod: Color,
    val predicted: Color,
    val onPredicted: Color,
    val selection: Color
)

@Composable
internal fun calendarColors(): CalendarColors = if (isSystemInDarkTheme()) {
    CalendarColors(
        surface = CalendarSurfaceDark,
        period = PeriodPinkDark,
        onPeriod = OnPeriodPink,
        softPeriod = PeriodPinkSoftDark,
        onSoftPeriod = OnPeriodPinkSoftDark,
        predicted = PredictedPinkDark,
        onPredicted = OnPredictedPinkDark,
        selection = SelectionPinkDark
    )
} else {
    CalendarColors(
        surface = CalendarSurface,
        period = PeriodPink,
        onPeriod = OnPeriodPink,
        softPeriod = PeriodPinkSoft,
        onSoftPeriod = OnPeriodPinkSoft,
        predicted = PredictedPink,
        onPredicted = OnPredictedPink,
        selection = SelectionPink
    )
}
