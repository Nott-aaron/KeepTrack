package com.example.keeptrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class RecordEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Basic information
    val title: String,
    val category: String,
    val subCategory: String? = null,

    // Product / item information
    val brand: String? = null,
    val model: String? = null,
    val serialNumber: String? = null,

    // Purchase information
    val purchaseDate: Long? = null,
    val purchasePrice: Double? = null,
    val storeOrProvider: String? = null,

    // Warranty / expiry information
    val warrantyPeriodMonths: Int? = null,
    val warrantyExpiryDate: Long? = null,
    val expiryDate: Long? = null,

    // Rental / agreement information
    val agreementStartDate: Long? = null,
    val agreementEndDate: Long? = null,
    val monthlyAmount: Double? = null,
    val securityDeposit: Double? = null,

    // Insurance information
    val policyNumber: String? = null,

    // Contact information
    val contactName: String? = null,
    val contactPhone: String? = null,

    // Document information
    val documentPath: String? = null,
    val documentType: String? = null,

    // Additional information
    val notes: String? = null,

    // App management
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)