package com.stephen.pinkdiary.data.prediction

import com.stephen.pinkdiary.data.local.PeriodRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class PeriodLogicTest {

    private fun record(id: Long, start: LocalDate, end: LocalDate? = null) = PeriodRecord(
        id = id,
        startDateEpochDay = start.toEpochDay(),
        endDateEpochDay = end?.toEpochDay(),
        createdAt = 0,
        updatedAt = 0
    )

    @Test
    fun `ongoing record is found`() {
        val ongoing = record(2, LocalDate.of(2026, 8, 1))
        val records = listOf(
            record(1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5)),
            ongoing
        )
        assertEquals(ongoing, PeriodLogic.ongoingRecord(records))
    }

    @Test
    fun `no ongoing returns null`() {
        val records = listOf(record(1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5)))
        assertNull(PeriodLogic.ongoingRecord(records))
    }

    @Test
    fun `covering record for completed period`() {
        val rec = record(1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5))
        val today = LocalDate.of(2026, 8, 10)
        assertEquals(rec, PeriodLogic.coveringRecord(listOf(rec), LocalDate.of(2026, 7, 3), today))
    }

    @Test
    fun `covering record for ongoing uses today as end`() {
        val rec = record(1, LocalDate.of(2026, 8, 1))
        val today = LocalDate.of(2026, 8, 4)
        assertEquals(rec, PeriodLogic.coveringRecord(listOf(rec), LocalDate.of(2026, 8, 4), today))
        // 未来日期不被进行中的记录覆盖
        assertNull(PeriodLogic.coveringRecord(listOf(rec), LocalDate.of(2026, 8, 5), today))
    }

    @Test
    fun `date outside any record is not covered`() {
        val records = listOf(record(1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5)))
        assertNull(PeriodLogic.coveringRecord(records, LocalDate.of(2026, 7, 6), LocalDate.of(2026, 8, 10)))
    }
}
