package com.example.keeptrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.keeptrack.data.model.RecordEntity
import com.example.keeptrack.data.repository.RecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecordViewModel(
    private val repository: RecordRepository
) : ViewModel() {

    // ---------------------------------------------------------
    // RECORD FLOWS
    // ---------------------------------------------------------

    val allRecords: Flow<List<RecordEntity>> =
        repository.allRecords

    val favoriteRecords: Flow<List<RecordEntity>> =
        repository.favoriteRecords

    val warrantyRecords: Flow<List<RecordEntity>> =
        repository.warrantyRecords

    val expiringRecords: Flow<List<RecordEntity>> =
        repository.expiringRecords


    // ---------------------------------------------------------
    // SELECTED RECORD
    // ---------------------------------------------------------

    private val _selectedRecord =
        MutableStateFlow<RecordEntity?>(null)

    val selectedRecord: StateFlow<RecordEntity?> =
        _selectedRecord.asStateFlow()


    // ---------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------

    private val _searchQuery =
        MutableStateFlow("")

    val searchQuery: StateFlow<String> =
        _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }


    // ---------------------------------------------------------
    // ADD RECORD
    // ---------------------------------------------------------

    fun addRecord(
        record: RecordEntity
    ) {

        viewModelScope.launch {

            repository.insertRecord(
                record
            )
        }
    }


    // ---------------------------------------------------------
    // UPDATE RECORD
    // ---------------------------------------------------------

    fun updateRecord(
        record: RecordEntity
    ) {

        viewModelScope.launch {

            repository.updateRecord(
                record
            )

            // Keep selected record updated
            _selectedRecord.value =
                repository.getRecordById(record.id)
        }
    }


    // ---------------------------------------------------------
    // DELETE RECORD
    // ---------------------------------------------------------

    fun deleteRecord(
        record: RecordEntity
    ) {

        viewModelScope.launch {

            repository.deleteRecord(
                record
            )

            if (_selectedRecord.value?.id == record.id) {
                _selectedRecord.value = null
            }
        }
    }

    // ---------------------------------------------------------
// DELETE ALL RECORDS
// ---------------------------------------------------------

    fun deleteAllRecords() {

        viewModelScope.launch {

            repository.deleteAllRecords()

            _selectedRecord.value = null
        }
    }

    // ---------------------------------------------------------
    // TOGGLE FAVORITE
    // ---------------------------------------------------------

    fun toggleFavorite(
        record: RecordEntity
    ) {

        viewModelScope.launch {

            val updatedRecord =
                record.copy(
                    isFavorite =
                        !record.isFavorite,

                    updatedAt =
                        System.currentTimeMillis()
                )

            repository.updateRecord(
                updatedRecord
            )

            if (_selectedRecord.value?.id == record.id) {
                _selectedRecord.value =
                    updatedRecord
            }
        }
    }


    // ---------------------------------------------------------
    // LOAD RECORD
    // ---------------------------------------------------------

    fun loadRecord(
        id: Long
    ) {

        viewModelScope.launch {

            _selectedRecord.value =
                repository.getRecordById(id)
        }
    }


    // ---------------------------------------------------------
    // CLEAR SELECTED RECORD
    // ---------------------------------------------------------

    fun clearSelectedRecord() {

        _selectedRecord.value = null
    }


    // ---------------------------------------------------------
    // GET RECORD BY ID
    // ---------------------------------------------------------

    suspend fun getRecordById(
        id: Long
    ): RecordEntity? {

        return repository.getRecordById(
            id
        )
    }


    // ---------------------------------------------------------
    // SEARCH RECORDS
    // ---------------------------------------------------------

    fun searchRecords(
        query: String
    ): Flow<List<RecordEntity>> {

        _searchQuery.value =
            query

        return repository.searchRecords(
            query
        )
    }


    // ---------------------------------------------------------
    // CATEGORY FILTER
    // ---------------------------------------------------------

    fun getRecordsByCategory(
        category: String
    ): Flow<List<RecordEntity>> {

        return repository.getRecordsByCategory(
            category
        )
    }

    suspend fun getAllRecordsOnce(): List<RecordEntity> = repository.getAllRecordsOnce()

    suspend fun replaceAllRecords(records: List<RecordEntity>) {
        repository.deleteAllRecords()
        repository.insertRecords(records)
        _selectedRecord.value = null
    }
}


// =============================================================
// VIEWMODEL FACTORY
// =============================================================

class RecordViewModelFactory(
    private val repository: RecordRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                RecordViewModel::class.java
            )
        ) {

            return RecordViewModel(
                repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}