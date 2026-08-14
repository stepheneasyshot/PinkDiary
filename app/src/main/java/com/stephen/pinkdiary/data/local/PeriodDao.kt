package com.stephen.pinkdiary.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodDao {

    @Query("SELECT * FROM period_records ORDER BY startDateEpochDay ASC")
    fun observeAll(): Flow<List<PeriodRecord>>

    @Query("SELECT * FROM period_records ORDER BY startDateEpochDay ASC")
    suspend fun getAll(): List<PeriodRecord>

    @Query("SELECT * FROM period_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PeriodRecord?

    @Query("SELECT * FROM period_records WHERE startDateEpochDay = :start LIMIT 1")
    suspend fun getByStart(start: Long): PeriodRecord?

    @Query("SELECT * FROM period_records WHERE endDateEpochDay IS NULL ORDER BY startDateEpochDay DESC LIMIT 1")
    suspend fun getOngoing(): PeriodRecord?

    @Insert
    suspend fun insert(record: PeriodRecord): Long

    @Update
    suspend fun update(record: PeriodRecord)

    @Delete
    suspend fun delete(record: PeriodRecord)
}
