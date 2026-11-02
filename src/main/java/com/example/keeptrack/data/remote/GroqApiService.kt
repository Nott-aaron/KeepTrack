package com.example.keeptrack.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// =============================================================
// GROQ API SERVICE
// =============================================================

interface GroqApiService {

    @POST("chat/completions")
    suspend fun analyzeReceipt(
        @Header("Authorization")
        authorization: String,

        @Body
        request: GroqRequest
    ): GroqResponse
}