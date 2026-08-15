package com.stephen.pinkdiary.ui.calendar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.stephen.pinkdiary.ui.theme.CalendarSurface
import com.stephen.pinkdiary.ui.theme.CalendarSurfaceDark
import com.stephen.pinkdiary.ui.theme.FollicularTint
import com.stephen.pinkdiary.ui.theme.FollicularTintDark
import com.stephen.pinkdiary.ui.theme.LutealTint
import com.stephen.pinkdiary.ui.theme.LutealTintDark
import com.stephen.pinkdiary.ui.theme.OnPeriodPink
import com.stephen.pinkdiary.ui.theme.OnPeriodPinkSoft
import com.stephen.pinkdiary.ui.theme.OnPeriodPinkSoftDark
import com.stephen.pinkdiary.ui.theme.OnPredictedPink
import com.stephen.pinkdiary.ui.theme.OnPredictedPinkDark
import com.stephen.pinkdiary.ui.theme.PeriodPink
import com.stephen.pinkdiary.ui.theme.PeriodPinkDark
import com.stephen.pinkdiary.ui.theme.PeriodPinkSoft
import com.stephen.pinkdiary.ui.theme.PeriodPinkSoftDark
import com.stephen.pinkdiary.ui.theme.PredictedPeriodFill
import com.stephen.pinkdiary.ui.theme.PredictedPeriodFillDark
import com.stephen.pinkdiary.ui.theme.OvulationTint
import com.stephen.pinkdiary.ui.theme.OvulationTintDark
import com.stephen.pinkdiary.ui.theme.SelectionPink
import com.stephen.pinkdiary.ui.theme.SelectionPinkDark

@Immutable
internal data class CalendarColors(
    val surface: Color,
    val period: Color,
    val onPeriod: Color,
    val softPeriod: Color,
    val onSoftPeriod: Color,
    val predictedPeriod: Color,
    val onPredicted: Color,
    val follicular: Color,
    val ovulation: Color,
    val luteal: Color,
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
        predictedPeriod = PredictedPeriodFillDark,
        onPredicted = OnPredictedPinkDark,
        follicular = FollicularTintDark,
        ovulation = OvulationTintDark,
        luteal = LutealTintDark,
        selection = SelectionPinkDark
    )
} else {
    CalendarColors(
        surface = CalendarSurface,
        period = PeriodPink,
        onPeriod = OnPeriodPink,
        softPeriod = PeriodPinkSoft,
        onSoftPeriod = OnPeriodPinkSoft,
        predictedPeriod = PredictedPeriodFill,
        onPredicted = OnPredictedPink,
        follicular = FollicularTint,
        ovulation = OvulationTint,
        luteal = LutealTint,
        selection = SelectionPink
    )
}
