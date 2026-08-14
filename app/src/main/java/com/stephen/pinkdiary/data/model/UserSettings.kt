package com.stephen.pinkdiary.data.model

/**
 * 用户设置：默认周期长度 / 默认经期长度 / 参与平均的最近周期数。
 * 无历史数据（冷启动）时，预测使用默认周期与经期长度。
 */
data class UserSettings(
    val defaultCycleLength: Int = 28,
    val defaultPeriodLength: Int = 5,
    val recentN: Int = 6
)
