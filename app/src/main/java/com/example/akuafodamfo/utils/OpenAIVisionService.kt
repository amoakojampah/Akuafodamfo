// OpenAIVisionService.kt
package com.example.akuafodamfo.utils

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIVisionService {
    @POST("chat/completions")
    fun analyzeImage(
        @Header("Authorization") authHeader: String,
        @Body requestBody: okhttp3.RequestBody
    ): Call<OpenAIResponse>
}