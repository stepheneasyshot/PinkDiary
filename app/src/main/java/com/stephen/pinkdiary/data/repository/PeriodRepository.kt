package com.stephen.pinkdiary.data.repository

import com.stephen.pinkdiary.data.local.PeriodDao
import com.stephen.pinkdiary.data.local.PeriodRecord
import kotlinx.coroutines.flow.Flow

class PeriodRepository(private val dao: PeriodDao) {

    val records: Flow<List<PeriodRecord>> = dao.observeAll()

    suspend fun getAll(): List<PeriodRecord> = dao.getAll()

    suspend fun getById(id: Long): PeriodRecord? = dao.getById(id)

    suspend fun getOngoing(): PeriodRecord? = dao.getOngoing()

    /**
     * 标记经期开始。
     * - 同日开始日已存在 → 视为修改，返回其 id；
     * - 存在进行中的经期且新开始日更晚 → 自动闭合上一次经期（结束日 = 新开始日 - 1）。
     */
    suspend fun markPeriodStart(startEpochDay: Long): Long {
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
     * @throws IllegalArgumentException 当结束日早于开始日
     */
    suspend fun markPeriodEnd(recordId: Long, endEpochDay: Long) {
        val record = dao.getById(recordId) ?: return
        require(endEpochDay >= record.startDateEpochDay) { "结束日不能早于开始日" }
        dao.update(record.copy(endDateEpochDay = endEpochDay, updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateNote(recordId: Long, note: String?) {
        val record = dao.getById(recordId) ?: return
        dao.update(record.copy(note = note, updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(record: PeriodRecord) = dao.delete(record)

    suspend fun deleteById(id: Long) {
        dao.getById(id)?.let { dao.delete(it) }
    }
}
