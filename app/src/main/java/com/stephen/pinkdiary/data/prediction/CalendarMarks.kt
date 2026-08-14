package com.stephen.pinkdiary.data.prediction

import com.stephen.pinkdiary.data.local.PeriodRecord
import java.time.LocalDate

/**
 * 日历标记纯函数：把记录与预测结果换算成「日期集合」，供日历渲染使用。
 */
object CalendarMarks {

    /**
     * 已记录经期日集合：每条记录从开始日到结束日（含首尾）；
     * 结束日为 null（进行中）时算到 [today]。
     */
    fun recordedPeriodDates(records: List<PeriodRecord>, today: LocalDate): Set<LocalDate> {
        val result = mutableSetOf<LocalDate>()
        records.forEach { r ->
            val start = LocalDate.ofEpochDay(r.startDateEpochDay)
            val end = r.endDateEpochDay?.let { LocalDate.ofEpochDay(it) } ?: today
            var d = start
            while (!d.isAfter(end)) {
                result.add(d)
                d = d.plusDays(1)
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
