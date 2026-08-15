package com.stephen.pinkdiary.data.prediction

import com.stephen.pinkdiary.data.local.PeriodRecord
import java.time.LocalDate

/**
 * 记录查询纯函数，供日历记录操作区判断上下文。
 */
object PeriodLogic {

    /** 进行中的经期记录（endDate == null），正常最多一条。 */
    fun ongoingRecord(records: List<PeriodRecord>): PeriodRecord? =
        records.firstOrNull { it.endDateEpochDay == null }

    /**
     * 覆盖指定日期的记录：start <= date <= (end ?: today)。
     * 未来日期不会被进行中的记录覆盖（因其结束日暂计为 today）。
     */
    fun coveringRecord(
        records: List<PeriodRecord>,
        date: LocalDate,
        today: LocalDate
    ): PeriodRecord? = records.firstOrNull { r ->
        val start = LocalDate.ofEpochDay(r.startDateEpochDay)
        val end = r.endDateEpochDay?.let { LocalDate.ofEpochDay(it) } ?: today
        !date.isBefore(start) && !date.isAfter(end)
    }
}
