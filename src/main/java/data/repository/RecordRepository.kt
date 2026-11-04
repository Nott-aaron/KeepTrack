package com.example.keeptrack.data.repository

import com.example.keeptrack.data.local.RecordDao
import com.example.keeptrack.data.model.RecordEntity
import kotlinx.coroutines.flow.Flow

class RecordRepository(
    private val recordDao: RecordDao
) {

    // =========================================================
    // RECORD FLOWS
    // =========================================================

    val allRecords: Flow<List<RecordEntity>> =
        recordDao.getAllRecords()

    val favoriteRecords: Flow<List<RecordEntity>> =
        recordDao.getFavoriteRecords()

    val warrantyRecords: Flow<List<RecordEntity>> =
        recordDao.getWarrantyRecords()

    val expiringRecords: Flow<List<RecordEntity>> =
        recordDao.getExpiringRecords()

    // =========================================================
    // INSERT
    // =========================================================

    suspend fun insertRecord(
        record: RecordEntity
    ) {
        recordDao.insertRecord(record)
    }

    suspend fun insertRecords(records: List<RecordEntity>) {
        recordDao.insertRecords(records)
    }

    suspend fun getAllRecordsOnce(): List<RecordEntity> = recordDao.getAllRecordsOnce()

    // =========================================================
    // UPDATE
    // =========================================================

    suspend fun updateRecord(
        record: RecordEntity
    ) {
        recordDao.updateRecord(record)
    }

    // =========================================================
    // DELETE ONE
    // =========================================================

    suspend fun deleteRecord(
        record: RecordEntity
    ) {
        recordDao.deleteRecord(record)
    }

    // =========================================================
    // DELETE ALL
    // =========================================================

    suspend fun deleteAllRecords() {
        recordDao.deleteAllRecords()
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    suspend fun getRecordById(
        id: Long
    ): RecordEntity? {

        return recordDao.getRecordById(id)
    }

    // =========================================================
    // SEARCH
    // =========================================================

    fun searchRecords(
        query: String
    ): Flow<List<RecordEntity>> {

        return recordDao.searchRecords(query)
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    fun getRecordsByCategory(
        category: String
    ): Flow<List<RecordEntity>> {

        return recordDao.getRecordsByCategory(category)
    }
}