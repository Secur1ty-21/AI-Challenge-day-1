package ru.yamost.first.agent.featute.chat.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import ru.yamost.first.agent.featute.chat.data.network.model.GetAnswerRequest
import ru.yamost.first.agent.featute.chat.data.network.model.GetAnswerResponse
import java.util.UUID

interface GptService {
    @POST("chat/completions")
    suspend fun getAnswer(
        @Header("X-Client-ID") clientId: String = UUID.randomUUID().toString(),
        @Header("X-Request-ID") requestId: String = UUID.randomUUID().toString(),
        @Header("X-Session-ID") sessionId: String = UUID.randomUUID().toString(),
        @Header("Authorization") bearerToken: String,
        @Body body: GetAnswerRequest
    ): Response<GetAnswerResponse>
}