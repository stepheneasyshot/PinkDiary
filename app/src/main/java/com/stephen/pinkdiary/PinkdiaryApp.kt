package com.stephen.pinkdiary

import android.app.Application
import com.stephen.pinkdiary.data.local.AppDatabase
import com.stephen.pinkdiary.data.repository.DataStoreUserSettingsRepository
import com.stephen.pinkdiary.data.repository.PeriodRepository
import com.stephen.pinkdiary.data.repository.RoomPeriodRepository
import com.stephen.pinkdiary.data.repository.UserSettingsRepository

/**
 * 应用级手动依赖容器（MVP 阶段暂不引入 Hilt）。
 */
class PinkdiaryApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val periodRepository: PeriodRepository by lazy { RoomPeriodRepository(database.periodDao()) }

    val userSettingsRepository: UserSettingsRepository by lazy { DataStoreUserSettingsRepository(this) }
}
