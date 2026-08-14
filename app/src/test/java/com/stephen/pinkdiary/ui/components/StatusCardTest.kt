package com.stephen.pinkdiary.ui.components

import com.stephen.pinkdiary.data.prediction.CyclePrediction
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StatusCardTest {

    private fun prediction(
        isOnPeriod: Boolean = false,
        daysUntilNext: Int = 20,
        periodDay: Int? = if (isOnPeriod) 3 else null
    ) = CyclePrediction(
        predictedStart = LocalDate.of(2026, 8, 20),
        predictedEnd = LocalDate.of(2026, 8, 24),
        durationDays = 5,
        averageCycleLength = 28,
        averagePeriodLength = 5,
        cycleDay = 10,
        daysUntilNext = daysUntilNext,
        ongoingPeriodStart = if (isOnPeriod) LocalDate.of(2026, 8, 1) else null,
        periodDay = periodDay
    )

    @Test
    fun `cold start`() {
        assertEquals(StatusKind.COLD_START, statusKind(null))
    }

    @Test
    fun `on period`() {
        assertEquals(StatusKind.ON_PERIOD, statusKind(prediction(isOnPeriod = true)))
    }

    @Test
    fun `overdue`() {
        assertEquals(StatusKind.OVERDUE, statusKind(prediction(daysUntilNext = -5)))
    }

    @Test
    fun `due today`() {
        assertEquals(StatusKind.DUE_TODAY, statusKind(prediction(daysUntilNext = 0)))
    }

    @Test
    fun `upcoming`() {
        assertEquals(StatusKind.UPCOMING, statusKind(prediction(daysUntilNext = 20)))
    }
}
