package com.example.keeptrack.data.remote

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class GroqRepository(
    private val context: Context,
    private val apiKey: String
) {

    private val api = GroqClient.api

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    suspend fun analyzeReceipt(
        imageUri: Uri,
        ocrText: String? = null
    ): Result<GroqReceiptData> {

        return runCatching {

            // =====================================================
            // 0. API KEY CHECK & SANITIZATION
            // =====================================================

            val cleanApiKey = apiKey.trim().removeSurrounding("\"")
            if (cleanApiKey.isBlank()) {
                throw IllegalArgumentException("GROQ_API_KEY is missing or empty in local.properties.")
            }

            val authHeader = if (cleanApiKey.startsWith("Bearer ", ignoreCase = true)) {
                cleanApiKey
            } else {
                "Bearer $cleanApiKey"
            }

            // =====================================================
            // 1. READ & SCALE RECEIPT IMAGE FOR AI
            // =====================================================

            val imageBytes = context.contentResolver.openInputStream(imageUri)?.use { input ->
                val originalBitmap = android.graphics.BitmapFactory.decodeStream(input)
                if (originalBitmap != null) {
                    val maxDimension = 1024
                    val width = originalBitmap.width
                    val height = originalBitmap.height
                    val scale = if (width > maxDimension || height > maxDimension) {
                        maxDimension.toFloat() / Math.max(width, height)
                    } else 1.0f

                    val scaledBitmap = if (scale < 1.0f) {
                        android.graphics.Bitmap.createScaledBitmap(
                            originalBitmap,
                            (width * scale).toInt(),
                            (height * scale).toInt(),
                            true
                        )
                    } else {
                        originalBitmap
                    }

                    val outputStream = java.io.ByteArrayOutputStream()
                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                    outputStream.toByteArray()
                } else {
                    null
                }
            } ?: error("Unable to read or decode receipt image.")


            // =====================================================
            // 2. CONVERT IMAGE TO BASE64
            // =====================================================

            val base64Image = Base64.encodeToString(
                imageBytes,
                Base64.NO_WRAP
            )


            // =====================================================
            // 3. CREATE IMAGE DATA URL
            // =====================================================

            val imageDataUrl = "data:image/jpeg;base64,$base64Image"


            // =====================================================
            // 4. AI INSTRUCTION
            // =====================================================

            val ocrContext = if (!ocrText.isNullOrBlank()) {
                "\n\nRaw text extracted from this document:\n$ocrText\n"
            } else ""

            val prompt = """
                Analyze this document or receipt carefully.$ocrContext

                Extract the following information:

                - storeName (Store, Vendor, Merchant, Organization, or Issuing Authority)
                - productName (Title of product, document name, card type, or main item description)
                - category (Select best fit: Electronics, Home Appliance, Machines & Equipment, Vehicles, Rental / Property, Insurance, Documents, Service & Maintenance, or Other)
                - brand
                - model
                - serialNumber
                - purchaseDate (Date in dd/MM/yyyy or yyyy-MM-dd format if available)
                - purchasePrice (Numeric amount only)
                - warrantyPeriodMonths (Integer number of months if mentioned)
                - notes (Summary of key text, document content, or important details)

                Rules:

                1. Return ONLY valid JSON matching the exact structure below.
                2. Do not use markdown backticks or explanations.
                3. Do not leave productName, storeName, or notes empty if text is visible in the document or OCR text.
                4. purchasePrice must be a number or null.
                5. warrantyPeriodMonths must be an integer or null.

                Return exactly this structure:

                {
                  "storeName": null,
                  "productName": null,
                  "category": null,
                  "brand": null,
                  "model": null,
                  "serialNumber": null,
                  "purchaseDate": null,
                  "purchasePrice": null,
                  "warrantyPeriodMonths": null,
                  "notes": null
                }
            """.trimIndent()


            // =====================================================
            // 5. CREATE REQUEST (Primary Model: qwen/qwen3.8-27b)
            // =====================================================

            val primaryModel = "qwen/qwen3.8-27b"
            val fallbackModel = "qwen/qwen3.8-27b"

            fun createRequest(modelName: String) = GroqRequest(
                model = modelName,
                messages = listOf(
                    GroqMessage(
                        role = "user",
                        content = listOf(
                            GroqContent(
                                type = "text",
                                text = prompt
                            ),
                            GroqContent(
                                type = "image_url",
                                imageUrl = GroqImageUrl(
                                    url = imageDataUrl
                                )
                            )
                        )
                    )
                ),
                temperature = 0.0
            )


            // =====================================================
            // 6. SEND TO GROQ WITH ERROR & FALLBACK HANDLING
            // =====================================================

            val response = try {
                api.analyzeReceipt(
                    authorization = authHeader,
                    request = createRequest(primaryModel)
                )
            } catch (e: HttpException) {
                if (e.code() == 404 || e.code() == 400) {
                    // Try fallback vision model if primary returns 404/400
                    try {
                        api.analyzeReceipt(
                            authorization = authHeader,
                            request = createRequest(fallbackModel)
                        )
                    } catch (fallbackEx: HttpException) {
                        throw parseHttpException(fallbackEx)
                    }
                } else {
                    throw parseHttpException(e)
                }
            }


            // =====================================================
            // 7. GET AI RESPONSE
            // =====================================================

            val responseText =
                response.choices
                    .firstOrNull()
                    ?.message
                    ?.content
                    ?.trim()
                    ?: error(
                        "Groq returned an empty response."
                    )


            // =====================================================
            // 8. CLEAN MARKDOWN IF PRESENT
            // =====================================================

            val cleanedJson =
                responseText
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()


            // =====================================================
            // 9. JSON → GROQ RECEIPT DATA
            // =====================================================

            json.decodeFromString<GroqReceiptData>(
                cleanedJson
            )
        }
    }

    private fun parseHttpException(e: HttpException): Exception {
        val code = e.code()
        val errorBody = e.response()?.errorBody()?.string() ?: ""
        val userMsg = when (code) {
            401 -> "HTTP 401 Unauthorized: Invalid or expired Groq API key. Check GROQ_API_KEY in local.properties."
            404 -> "HTTP 404 Not Found: Vision model not available on Groq API. $errorBody"
            429 -> "HTTP 429 Rate Limit Exceeded: Groq API quota reached. Please wait and retry."
            400 -> "HTTP 400 Bad Request: $errorBody"
            else -> "HTTP $code Error from Groq API: $errorBody"
        }
        return Exception(userMsg)
    }
}
