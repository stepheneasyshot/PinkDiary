package com.stephen.pinkdiary.ui.components

import com.stephen.pinkdiary.data.prediction.CyclePrediction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        val m = buildStatusCardModel(null)
        assertEquals(StatusKind.COLD_START, m.kind)
    }

    @Test
    fun `on period`() {
        val m = buildStatusCardModel(prediction(isOnPeriod = true))
        assertEquals(StatusKind.ON_PERIOD, m.kind)
        assertTrue(m.title.contains("第 3 天"))
        assertTrue(m.subtitle.contains("预计持续 5 天"))
    }

    @Test
    fun `overdue`() {
        val m = buildStatusCardModel(prediction(daysUntilNext = -5))
        assertEquals(StatusKind.OVERDUE, m.kind)
        assertTrue(m.title.contains("5 天"))
    }

    @Test
    fun `due today`() {
        val m = buildStatusCardModel(prediction(daysUntilNext = 0))
        assertEquals(StatusKind.DUE_TODAY, m.kind)
    }

    @Test
    fun `upcoming`() {
        val m = buildStatusCardModel(prediction(daysUntilNext = 20))
        assertEquals(StatusKind.UPCOMING, m.kind)
        assertTrue(m.title.contains("20 天"))
    }
}
