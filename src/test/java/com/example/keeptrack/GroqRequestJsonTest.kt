package com.example.keeptrack

import com.example.keeptrack.data.remote.GroqContent
import com.example.keeptrack.data.remote.GroqImageUrl
import com.example.keeptrack.data.remote.GroqMessage
import com.example.keeptrack.data.remote.GroqRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*

class GroqRequestJsonTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun testGroqRequestSerializationDoesNotIncludeExplicitNulls() {
        val request = GroqRequest(
            model = "qwen/qwen3.8-27b",
            messages = listOf(
                GroqMessage(
                    role = "user",
                    content = listOf(
                        GroqContent(
                            type = "text",
                            text = "Analyze this receipt"
                        ),
                        GroqContent(
                            type = "image_url",
                            imageUrl = GroqImageUrl(url = "data:image/jpeg;base64,12345")
                        )
                    )
                )
            ),
            temperature = 0.0
        )

        val jsonString = json.encodeToString(request)

        // Verify that null fields ("image_url": null and "text": null) are NOT encoded in the JSON payload
        assertFalse("JSON must not contain 'image_url': null", jsonString.contains("\"image_url\":null"))
        assertFalse("JSON must not contain 'text': null", jsonString.contains("\"text\":null"))
        assertTrue("JSON must contain model", jsonString.contains("\"model\":\"qwen/qwen3.8-27b\""))
        assertTrue("JSON must contain image_url object", jsonString.contains("\"url\":\"data:image/jpeg;base64,12345\""))
    }
}
