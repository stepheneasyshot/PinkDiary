package com.stephen.pinkdiary.data.prediction

import com.stephen.pinkdiary.data.local.PeriodRecord
import java.time.LocalDate

/**
 * 日历标记纯函数：把记录与预测结果换算成「日期集合」，供日历渲染使用。
 */
object CalendarMarks {

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
}
