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
    fun `completed period is solid over its full range`() {
        val dates = CalendarMarks.solidPeriodDates(
            listOf(record(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3)))
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
    fun `ongoing period marks only start day solid`() {
        val dates = CalendarMarks.solidPeriodDates(
            listOf(record(LocalDate.of(2026, 8, 1)))
        )
        assertEquals(setOf(LocalDate.of(2026, 8, 1)), dates)
    }

    @Test
    fun `ongoing period soft days span day after start to today`() {
        val dates = CalendarMarks.softPeriodDates(
            listOf(record(LocalDate.of(2026, 8, 1))),
            LocalDate.of(2026, 8, 4)
        )
        assertEquals(
            setOf(
                LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 4)
            ),
            dates
        )
    }

    @Test
    fun `ongoing started today has no soft days`() {
        val today = LocalDate.of(2026, 8, 4)
        assertTrue(CalendarMarks.softPeriodDates(listOf(record(today)), today).isEmpty())
    }

    @Test
    fun `multiple records are merged`() {
        val dates = CalendarMarks.solidPeriodDates(
            listOf(
                record(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 2)),
                record(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2))
            )
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
