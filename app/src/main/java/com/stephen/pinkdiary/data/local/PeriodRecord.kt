package com.stephen.pinkdiary.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 一次经期 = 一条记录（片段式建模）。
 * [endDateEpochDay] 为 null 表示经期进行中、尚未结束。
 */
@Entity(tableName = "period_records")
data class PeriodRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long?,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
