package co.edu.unal.reto11_genia

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("models/gemini-pro:generateContent")
    suspend fun generarImagen(
        @Query("key") apiKey: String,
        @Body request: RequestBody
    ): Response<GeminiResponse>
}

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: String?
)
