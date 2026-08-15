package com.stephen.pinkdiary.data.prediction

import com.stephen.pinkdiary.data.local.PeriodRecord
import java.time.LocalDate

enum class CyclePhase {
    FOLLICULAR,
    OVULATION,
    LUTEAL
}

data class CyclePhaseDates(
    val follicular: Set<LocalDate> = emptySet(),
    val ovulation: Set<LocalDate> = emptySet(),
    val luteal: Set<LocalDate> = emptySet()
) {
    fun phaseOn(date: LocalDate): CyclePhase? = when (date) {
        in follicular -> CyclePhase.FOLLICULAR
        in ovulation -> CyclePhase.OVULATION
        in luteal -> CyclePhase.LUTEAL
        else -> null
    }
}

/**
 * 日历标记纯函数：把记录与预测结果换算成「日期集合」，供日历渲染使用。
 */
object CalendarMarks {

    /** 以下次经期开始日倒推的默认黄体期长度。 */
    const val ESTIMATED_LUTEAL_DAYS = 14L

    /**
     * 实心标记日：已结束经期的全程（含首尾）+ 进行中经期的开始日。
     */
    fun solidPeriodDates(records: List<PeriodRecord>): Set<LocalDate> {
        val result = mutableSetOf<LocalDate>()
        records.forEach { r ->
            val start = LocalDate.ofEpochDay(r.startDateEpochDay)
            val end = r.endDateEpochDay?.let { LocalDate.ofEpochDay(it) }
            if (end != null) {
                var d = start
                while (!d.isAfter(end)) {
                    result.add(d)
                    d = d.plusDays(1)
                }
            } else {
                result.add(start) // 进行中：仅开始日实心
            }
        }
        return result
    }

    /**
     * 温和标记日：进行中经期从开始日次日到今天（不含开始日）。
     */
    fun softPeriodDates(records: List<PeriodRecord>, today: LocalDate): Set<LocalDate> {
        val result = mutableSetOf<LocalDate>()
        records.forEach { r ->
            if (r.endDateEpochDay == null) {
                val start = LocalDate.ofEpochDay(r.startDateEpochDay)
                var d = start.plusDays(1)
                while (!d.isAfter(today)) {
                    result.add(d)
                    d = d.plusDays(1)
                }
            }
        }
        return result
    }

    /**
     * 预测经期日集合：predictedStart..predictedEnd（含首尾）。
     */
    fun predictedPeriodDates(prediction: CyclePrediction?): Set<LocalDate> {
        val p = prediction ?: return emptySet()
        val result = mutableSetOf<LocalDate>()
        var d = p.predictedStart
        while (!d.isAfter(p.predictedEnd)) {
            result.add(d)
            d = d.plusDays(1)
        }
        return result
    }

    /**
     * 估算每个已知周期的非经期阶段。
     *
     * 排卵日按「下次经期开始日 - 14 天」估算；历史周期使用下一条真实开始日，
     * 当前周期使用 [CyclePrediction.predictedStart]。为避免与经期标记重叠，
     * UI 中的“卵泡期”从出血结束的次日开始。这些结果只是日历估算，不是排卵检测。
     */
    fun cyclePhaseDates(
        records: List<PeriodRecord>,
        prediction: CyclePrediction?,
        today: LocalDate
    ): CyclePhaseDates {
        val p = prediction ?: return CyclePhaseDates()
        val sorted = records.sortedBy { it.startDateEpochDay }
        if (sorted.isEmpty()) return CyclePhaseDates()

        val follicular = mutableSetOf<LocalDate>()
        val ovulation = mutableSetOf<LocalDate>()
        val luteal = mutableSetOf<LocalDate>()

        sorted.forEachIndexed { index, record ->
            val cycleStart = LocalDate.ofEpochDay(record.startDateEpochDay)
            val nextPeriodStart = sorted.getOrNull(index + 1)
                ?.let { LocalDate.ofEpochDay(it.startDateEpochDay) }
                ?: p.predictedStart
            if (!nextPeriodStart.isAfter(cycleStart)) return@forEachIndexed
            val cycleLength = (nextPeriodStart.toEpochDay() - cycleStart.toEpochDay()).toInt()
            if (cycleLength !in CyclePredictor.MIN_VALID_CYCLE..CyclePredictor.MAX_VALID_CYCLE) {
                return@forEachIndexed
            }

            val estimatedPeriodEnd = cycleStart.plusDays((p.averagePeriodLength - 1).toLong())
            val rawPeriodEnd = record.endDateEpochDay?.let(LocalDate::ofEpochDay)
                ?: if (index == sorted.lastIndex) maxOf(today, estimatedPeriodEnd) else estimatedPeriodEnd
            val periodEnd = minOf(rawPeriodEnd, nextPeriodStart.minusDays(1))
            val nonPeriodStart = periodEnd.plusDays(1)
            if (!nonPeriodStart.isBefore(nextPeriodStart)) return@forEachIndexed

            val estimatedOvulation = nextPeriodStart.minusDays(ESTIMATED_LUTEAL_DAYS)
            addRange(follicular, nonPeriodStart, minOf(estimatedOvulation.minusDays(1), nextPeriodStart.minusDays(1)))

            if (!estimatedOvulation.isBefore(nonPeriodStart) && estimatedOvulation.isBefore(nextPeriodStart)) {
                ovulation.add(estimatedOvulation)
            }

            addRange(
                luteal,
                maxOf(nonPeriodStart, estimatedOvulation.plusDays(1)),
                nextPeriodStart.minusDays(1)
            )
        }

        return CyclePhaseDates(follicular, ovulation, luteal)
    }

    private fun addRange(target: MutableSet<LocalDate>, start: LocalDate, end: LocalDate) {
        if (start.isAfter(end)) return
        var date = start
        while (!date.isAfter(end)) {
            target.add(date)
            date = date.plusDays(1)
        }
    }
}
