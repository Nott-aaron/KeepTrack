package com.example.keeptrack

import com.example.keeptrack.data.model.RecordEntity
import com.example.keeptrack.data.remote.GroqReceiptData
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*

class RecordEntityAndAiTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testRecordEntityCreation() {
        val record = RecordEntity(
            title = "Samsung Galaxy S24",
            category = "Electronics",
            brand = "Samsung",
            model = "S24 Ultra",
            purchasePrice = 1299.99,
            storeOrProvider = "Best Buy",
            warrantyPeriodMonths = 24,
            documentPath = "content://media/external/images/media/123"
        )

        assertEquals("Samsung Galaxy S24", record.title)
        assertEquals("Electronics", record.category)
        assertEquals("Samsung", record.brand)
        assertEquals("S24 Ultra", record.model)
        assertEquals(1299.99, record.purchasePrice!!, 0.001)
        assertEquals("Best Buy", record.storeOrProvider)
        assertEquals(24, record.warrantyPeriodMonths)
        assertEquals("content://media/external/images/media/123", record.documentPath)
        assertFalse(record.isFavorite)
    }

    @Test
    fun testGroqReceiptDataJsonParsing() {
        val sampleJson = """
            {
              "storeName": "Target",
              "productName": "Sony WH-1000XM5",
              "category": "Electronics",
              "brand": "Sony",
              "model": "WH-1000XM5",
              "serialNumber": "SN987654321",
              "purchaseDate": "12/10/2024",
              "purchasePrice": 399.99,
              "warrantyPeriodMonths": 12,
              "notes": "Purchased on black friday sale"
            }
        """.trimIndent()

        val receiptData = json.decodeFromString<GroqReceiptData>(sampleJson)

        assertEquals("Target", receiptData.storeName)
        assertEquals("Sony WH-1000XM5", receiptData.productName)
        assertEquals("Electronics", receiptData.category)
        assertEquals("Sony", receiptData.brand)
        assertEquals("WH-1000XM5", receiptData.model)
        assertEquals("SN987654321", receiptData.serialNumber)
        assertEquals("12/10/2024", receiptData.purchaseDate)
        assertEquals(399.99, receiptData.purchasePrice!!, 0.001)
        assertEquals(12, receiptData.warrantyPeriodMonths)
        assertEquals("Purchased on black friday sale", receiptData.notes)
    }
}
