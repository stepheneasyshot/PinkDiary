package com.stephen.pinkdiary.data.repository

import com.stephen.pinkdiary.data.local.PeriodDao
import com.stephen.pinkdiary.data.local.PeriodRecord
import kotlinx.coroutines.flow.Flow

/** 标记经期结束时，结束日早于开始日的非法状态。 */
class PeriodEndBeforeStartException : IllegalArgumentException()

interface PeriodRepository {
    val records: Flow<List<PeriodRecord>>

    suspend fun getAll(): List<PeriodRecord>
    suspend fun getById(id: Long): PeriodRecord?
    suspend fun getOngoing(): PeriodRecord?
    suspend fun markPeriodStart(startEpochDay: Long): Long
    suspend fun markPeriodEnd(recordId: Long, endEpochDay: Long)
    suspend fun updateNote(recordId: Long, note: String?)
    suspend fun delete(record: PeriodRecord)
    suspend fun deleteById(id: Long)
}

class RoomPeriodRepository(private val dao: PeriodDao) : PeriodRepository {

    override val records: Flow<List<PeriodRecord>> = dao.observeAll()

    override suspend fun getAll(): List<PeriodRecord> = dao.getAll()

    override suspend fun getById(id: Long): PeriodRecord? = dao.getById(id)

    override suspend fun getOngoing(): PeriodRecord? = dao.getOngoing()

    /**
     * 标记经期开始。
     * - 同日开始日已存在 → 视为修改，返回其 id；
     * - 存在进行中的经期且新开始日更晚 → 自动闭合上一次经期（结束日 = 新开始日 - 1）。
     */
    override suspend fun markPeriodStart(startEpochDay: Long): Long {
        val now = System.currentTimeMillis()
        dao.getByStart(startEpochDay)?.let { return it.id }

        dao.getOngoing()?.let { ongoing ->
            if (startEpochDay > ongoing.startDateEpochDay) {
                dao.update(ongoing.copy(endDateEpochDay = startEpochDay - 1, updatedAt = now))
            }
        }

        return dao.insert(
            PeriodRecord(
                startDateEpochDay = startEpochDay,
                endDateEpochDay = null,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    /**
     * 标记经期结束。
     * @throws PeriodEndBeforeStartException 当结束日早于开始日
     */
    override suspend fun markPeriodEnd(recordId: Long, endEpochDay: Long) {
        val record = dao.getById(recordId) ?: return
        if (endEpochDay < record.startDateEpochDay) throw PeriodEndBeforeStartException()
        dao.update(record.copy(endDateEpochDay = endEpochDay, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateNote(recordId: Long, note: String?) {
        val record = dao.getById(recordId) ?: return
        dao.update(record.copy(note = note, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun delete(record: PeriodRecord) = dao.delete(record)

    override suspend fun deleteById(id: Long) {
        dao.getById(id)?.let { dao.delete(it) }
    }
}
