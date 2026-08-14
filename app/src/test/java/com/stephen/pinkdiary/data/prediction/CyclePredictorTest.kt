package com.stephen.pinkdiary.data.prediction

import com.stephen.pinkdiary.data.local.PeriodRecord
import com.stephen.pinkdiary.data.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CyclePredictorTest {

    private val settings = UserSettings() // 28 / 5 / 6

    private fun record(start: LocalDate, end: LocalDate? = null) = PeriodRecord(
        id = 0,
        startDateEpochDay = start.toEpochDay(),
        endDateEpochDay = end?.toEpochDay(),
        createdAt = 0,
        updatedAt = 0
    )

    @Test
    fun `cold start returns null`() {
        val result = CyclePredictor.predict(emptyList(), settings, LocalDate.of(2026, 8, 1))
        assertNull(result)
    }

    @Test
    fun `single record uses default cycle and period length`() {
        val p = CyclePredictor.predict(
            listOf(record(LocalDate.of(2026, 7, 1))),
            settings,
            LocalDate.of(2026, 7, 10)
        )!!
        assertEquals(28, p.averageCycleLength)
        assertEquals(5, p.averagePeriodLength)
        assertEquals(LocalDate.of(2026, 7, 29), p.predictedStart)
        assertEquals(LocalDate.of(2026, 8, 2), p.predictedEnd)
        assertEquals(10, p.cycleDay)
        assertEquals(19, p.daysUntilNext)
    }

    @Test
    fun `average cycle length from history`() {
        val starts = listOf(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 29),
            LocalDate.of(2026, 2, 26),
            LocalDate.of(2026, 3, 26)
        )
        assertEquals(28, CyclePredictor.averageCycleLength(starts, 6, 28))
    }

    @Test
    fun `outliers outside 15-60 days are filtered`() {
        val starts = listOf(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 29), // 28，有效
            LocalDate.of(2026, 1, 31)  // 2 天，异常，被剔除
        )
        assertEquals(28, CyclePredictor.averageCycleLength(starts, 6, 28))
    }

    @Test
    fun `recentN limits the averaging window`() {
        val starts = listOf(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 29), // 28
            LocalDate.of(2026, 3, 29), // 60（有效）
            LocalDate.of(2026, 4, 28)  // 30
        )
        // 周期长度 [28, 60, 30]，recentN=2 只取 [60, 30] → 45
        assertEquals(45, CyclePredictor.averageCycleLength(starts, 2, 28))
    }

    @Test
    fun `period length averages only completed records`() {
        val records = listOf(
            record(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5)), // 5
            record(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 6)), // 6
            record(LocalDate.of(2026, 3, 1)) // 进行中，忽略
        )
        // (5 + 6) / 2 = 5.5 → 四舍五入 6
        assertEquals(6, CyclePredictor.averagePeriodLength(records, 5))
    }

    @Test
    fun `ongoing period is detected`() {
        val p = CyclePredictor.predict(
            listOf(record(LocalDate.of(2026, 8, 1))),
            settings,
            LocalDate.of(2026, 8, 4)
        )!!
        assertTrue(p.isOnPeriod)
        assertEquals(4, p.periodDay)
        assertEquals(LocalDate.of(2026, 8, 1), p.ongoingPeriodStart)
    }

    @Test
    fun `daysUntilNext is negative when prediction already passed`() {
        val p = CyclePredictor.predict(
            listOf(record(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5))),
            settings,
            LocalDate.of(2026, 7, 5)
        )!!
        assertTrue(p.daysUntilNext < 0)
        assertFalse(p.isOnPeriod)
    }
}
