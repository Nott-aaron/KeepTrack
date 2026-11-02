package com.example.keeptrack.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =============================================================
// GROQ CHAT COMPLETION REQUEST
// =============================================================

@Serializable
data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.0,
    @SerialName("max_tokens")
    val maxTokens: Int = 1024
)


// =============================================================
// MESSAGE
// =============================================================

@Serializable
data class GroqMessage(
    val role: String,
    val content: List<GroqContent>
)


// =============================================================
// MESSAGE CONTENT
// =============================================================

@Serializable
data class GroqContent(

    val type: String,

    val text: String? = null,

    @SerialName("image_url")
    val imageUrl: GroqImageUrl? = null
)


// =============================================================
// IMAGE URL
// =============================================================

@Serializable
data class GroqImageUrl(
    val url: String
)


// =============================================================
// GROQ RESPONSE
// =============================================================

@Serializable
data class GroqResponse(
    val choices: List<GroqChoice> = emptyList()
)


// =============================================================
// CHOICE
// =============================================================

@Serializable
data class GroqChoice(
    val message: GroqResponseMessage
)


// =============================================================
// RESPONSE MESSAGE
// =============================================================

@Serializable
data class GroqResponseMessage(
    val role: String? = null,
    val content: String? = null
)


// =============================================================
// RECEIPT DATA
// =============================================================

@Serializable
data class GroqReceiptData(

    val storeName: String? = null,

    val productName: String? = null,

    val category: String? = null,

    val brand: String? = null,

    val model: String? = null,

    val serialNumber: String? = null,

    val purchaseDate: String? = null,

    val purchasePrice: Double? = null,

    val warrantyPeriodMonths: Int? = null,

    val notes: String? = null
)
