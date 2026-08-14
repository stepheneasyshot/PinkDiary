package com.stephen.pinkdiary.data.prediction

import com.stephen.pinkdiary.data.local.PeriodRecord
import com.stephen.pinkdiary.data.model.UserSettings
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * 下次经期预测结果。
 */
data class CyclePrediction(
    val predictedStart: LocalDate,
    val predictedEnd: LocalDate,
    val durationDays: Int,
    val averageCycleLength: Int,
    val averagePeriodLength: Int,
    val cycleDay: Int,
    val daysUntilNext: Int,
    val ongoingPeriodStart: LocalDate? = null,
    val periodDay: Int? = null
) {
    val isOnPeriod: Boolean get() = ongoingPeriodStart != null
}

/**
 * 预测纯函数（无 Android / IO 依赖，可直接单测）。
 */
object CyclePredictor {

    const val MIN_VALID_CYCLE = 15
    const val MAX_VALID_CYCLE = 60

    fun sortedStartDates(records: List<PeriodRecord>): List<LocalDate> =
        records.map { LocalDate.ofEpochDay(it.startDateEpochDay) }.sorted()

    /**
     * 平均周期长度：相邻开始日之差，剔除 15~60 天之外的异常值，
     * 取最近 [recentN] 个的算术平均；无有效周期时回退 [defaultCycleLength]。
     */
    fun averageCycleLength(
        starts: List<LocalDate>,
        recentN: Int,
        defaultCycleLength: Int
    ): Int {
        val lengths = starts.sorted().zipWithNext { a, b ->
            (b.toEpochDay() - a.toEpochDay()).toInt()
        }.filter { it in MIN_VALID_CYCLE..MAX_VALID_CYCLE }

        if (lengths.isEmpty()) return defaultCycleLength

        val recent = if (lengths.size > recentN) lengths.takeLast(recentN) else lengths
        return recent.average().roundToInt()
    }

    /**
     * 平均经期长度：所有「已结束」记录的 (end - start + 1) 平均；
     * 无已结束记录时回退 [defaultPeriodLength]。
     */
    fun averagePeriodLength(records: List<PeriodRecord>, defaultPeriodLength: Int): Int {
        val durations = records.mapNotNull { r ->
            r.endDateEpochDay?.let { end -> (end - r.startDateEpochDay + 1).toInt() }
        }
        if (durations.isEmpty()) return defaultPeriodLength
        return durations.average().roundToInt()
    }

    /**
     * 预测下次经期。无任何历史记录时返回 null（冷启动，需先记录一次开始日）。
     * [daysUntilNext] 为负表示预测开始日已过但尚未记录。
     */
    fun predict(
        records: List<PeriodRecord>,
        settings: UserSettings,
        today: LocalDate
    ): CyclePrediction? {
        val starts = sortedStartDates(records)
        val lastStart = starts.lastOrNull() ?: return null

        val avgCycle = averageCycleLength(starts, settings.recentN, settings.defaultCycleLength)
        val avgPeriod = averagePeriodLength(records, settings.defaultPeriodLength)

        val predictedStart = lastStart.plusDays(avgCycle.toLong())
        val predictedEnd = predictedStart.plusDays((avgPeriod - 1).toLong())

        val cycleDay = (today.toEpochDay() - lastStart.toEpochDay()).toInt() + 1
        val daysUntilNext = (predictedStart.toEpochDay() - today.toEpochDay()).toInt()

        val ongoing = records.firstOrNull {
            it.endDateEpochDay == null && it.startDateEpochDay <= today.toEpochDay()
        }
        val periodDay = ongoing?.let { (today.toEpochDay() - it.startDateEpochDay).toInt() + 1 }

        return CyclePrediction(
            predictedStart = predictedStart,
            predictedEnd = predictedEnd,
            durationDays = avgPeriod,
            averageCycleLength = avgCycle,
            averagePeriodLength = avgPeriod,
            cycleDay = cycleDay,
            daysUntilNext = daysUntilNext,
            ongoingPeriodStart = ongoing?.let { LocalDate.ofEpochDay(it.startDateEpochDay) },
            periodDay = periodDay
        )
    }
}
