package com.stephen.pinkdiary.data.prediction

import com.stephen.pinkdiary.data.local.PeriodRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalendarMarksTest {

    private fun record(start: LocalDate, end: LocalDate? = null) = PeriodRecord(
        id = 0,
        startDateEpochDay = start.toEpochDay(),
        endDateEpochDay = end?.toEpochDay(),
        createdAt = 0,
        updatedAt = 0
    )

    @Test
    fun `recorded dates span start to end inclusive`() {
        val dates = CalendarMarks.recordedPeriodDates(
            listOf(record(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3))),
            LocalDate.of(2026, 8, 10)
        )
        assertEquals(
            setOf(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 3)
            ),
            dates
        )
    }

    @Test
    fun `ongoing record spans to today`() {
        val dates = CalendarMarks.recordedPeriodDates(
            listOf(record(LocalDate.of(2026, 8, 1))),
            LocalDate.of(2026, 8, 4)
        )
        assertEquals(
            setOf(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 4)
            ),
            dates
        )
    }

    @Test
    fun `multiple records are merged into one set`() {
        val dates = CalendarMarks.recordedPeriodDates(
            listOf(
                record(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 2)),
                record(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2))
            ),
            LocalDate.of(2026, 8, 10)
        )
        assertEquals(4, dates.size)
        assertTrue(LocalDate.of(2026, 7, 1) in dates)
        assertTrue(LocalDate.of(2026, 8, 2) in dates)
    }

    @Test
    fun `predicted dates span predicted range`() {
        val prediction = CyclePrediction(
            predictedStart = LocalDate.of(2026, 8, 20),
            predictedEnd = LocalDate.of(2026, 8, 24),
            durationDays = 5,
            averageCycleLength = 28,
            averagePeriodLength = 5,
            cycleDay = 1,
            daysUntilNext = 0
        )
        val dates = CalendarMarks.predictedPeriodDates(prediction)
        assertEquals(
            (0..4).map { LocalDate.of(2026, 8, 20).plusDays(it.toLong()) }.toSet(),
            dates
        )
    }

    @Test
    fun `null prediction yields empty set`() {
        assertTrue(CalendarMarks.predictedPeriodDates(null).isEmpty())
    }
}
