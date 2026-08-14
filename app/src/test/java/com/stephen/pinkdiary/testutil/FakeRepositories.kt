package com.stephen.pinkdiary.testutil

import com.stephen.pinkdiary.data.local.PeriodRecord
import com.stephen.pinkdiary.data.model.UserSettings
import com.stephen.pinkdiary.data.repository.PeriodRepository
import com.stephen.pinkdiary.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakePeriodRepository(
    initialRecords: List<PeriodRecord> = emptyList()
) : PeriodRepository {
    private val mutableRecords = MutableStateFlow(initialRecords)
    override val records = mutableRecords.asStateFlow()

    var failNextWrite = false
    var lastMarkedStart: Long? = null

    override suspend fun getAll(): List<PeriodRecord> = records.value
    override suspend fun getById(id: Long): PeriodRecord? = records.value.find { it.id == id }
    override suspend fun getOngoing(): PeriodRecord? = records.value.find { it.endDateEpochDay == null }

    override suspend fun markPeriodStart(startEpochDay: Long): Long {
        failIfRequested()
        lastMarkedStart = startEpochDay
        return 1L
    }

    override suspend fun markPeriodEnd(recordId: Long, endEpochDay: Long) {
        failIfRequested()
    }

    override suspend fun updateNote(recordId: Long, note: String?) = Unit
    override suspend fun delete(record: PeriodRecord) = Unit

    override suspend fun deleteById(id: Long) {
        failIfRequested()
    }

    private fun failIfRequested() {
        if (failNextWrite) {
            failNextWrite = false
            error("write failed")
        }
    }
}

class FakeUserSettingsRepository(
    initialSettings: UserSettings = UserSettings(),
    onboardingDone: Boolean = false
) : UserSettingsRepository {
    private val mutableSettings = MutableStateFlow(initialSettings)
    override val settings = mutableSettings.asStateFlow()

    private val mutableOnboardingCompleted = MutableStateFlow(onboardingDone)
    override val onboardingCompleted = mutableOnboardingCompleted.asStateFlow()

    var lastUpdatedSettings: UserSettings? = null
    var failOnboardingCompletion = false

    override suspend fun update(settings: UserSettings) {
        lastUpdatedSettings = settings
        mutableSettings.value = settings
    }

    override suspend fun setOnboardingCompleted() {
        if (failOnboardingCompletion) error("write failed")
        mutableOnboardingCompleted.value = true
    }
}
