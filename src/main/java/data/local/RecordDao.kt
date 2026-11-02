package com.example.keeptrack.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.keeptrack.data.model.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    // =========================================================
    // INSERT
    // =========================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(
        record: RecordEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<RecordEntity>)

    // =========================================================
    // UPDATE
    // =========================================================

    @Update
    suspend fun updateRecord(
        record: RecordEntity
    )

    // =========================================================
    // DELETE ONE
    // =========================================================

    @Delete
    suspend fun deleteRecord(
        record: RecordEntity
    )

    // =========================================================
    // DELETE ALL
    // =========================================================

    @Query("DELETE FROM records")
    suspend fun deleteAllRecords()

    // =========================================================
    // GET ALL
    // =========================================================

    @Query(
        "SELECT * FROM records ORDER BY createdAt DESC"
    )
    fun getAllRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records ORDER BY createdAt DESC")
    suspend fun getAllRecordsOnce(): List<RecordEntity>

    // =========================================================
    // GET BY ID
    // =========================================================

    @Query(
        "SELECT * FROM records WHERE id = :recordId LIMIT 1"
    )
    suspend fun getRecordById(
        recordId: Long
    ): RecordEntity?

    // =========================================================
    // SEARCH
    // =========================================================

    @Query(
        """
        SELECT * FROM records
        WHERE title LIKE '%' || :query || '%'
        OR category LIKE '%' || :query || '%'
        OR brand LIKE '%' || :query || '%'
        OR model LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun searchRecords(
        query: String
    ): Flow<List<RecordEntity>>

    // =========================================================
    // CATEGORY
    // =========================================================

    @Query(
        """
        SELECT * FROM records
        WHERE category = :category
        ORDER BY createdAt DESC
        """
    )
    fun getRecordsByCategory(
        category: String
    ): Flow<List<RecordEntity>>

    // =========================================================
    // FAVORITES
    // =========================================================

    @Query(
        """
        SELECT * FROM records
        WHERE isFavorite = 1
        ORDER BY createdAt DESC
        """
    )
    fun getFavoriteRecords(): Flow<List<RecordEntity>>

    // =========================================================
    // WARRANTY
    // =========================================================

    @Query(
        """
        SELECT * FROM records
        WHERE warrantyExpiryDate IS NOT NULL
        ORDER BY warrantyExpiryDate ASC
        """
    )
    fun getWarrantyRecords(): Flow<List<RecordEntity>>

    // =========================================================
    // EXPIRING
    // =========================================================

    @Query(
        """
        SELECT * FROM records
        WHERE expiryDate IS NOT NULL
        ORDER BY expiryDate ASC
        """
    )
    fun getExpiringRecords(): Flow<List<RecordEntity>>
}