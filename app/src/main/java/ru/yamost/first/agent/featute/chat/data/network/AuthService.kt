package ru.yamost.first.agent.featute.chat.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import ru.yamost.first.agent.BuildConfig
import ru.yamost.first.agent.featute.chat.data.network.model.GetAccessTokenResponse
import java.util.UUID

interface AuthService {
    @FormUrlEncoded
    @POST("oauth")
    suspend fun getAccessToken(
        @Header("Authorization") basicToken: String = "Basic ${BuildConfig.MODEL_API_KEY}",
        @Header("RqUID") uid: String = UUID.randomUUID().toString(),
        @Field("scope") scope: String = "GIGACHAT_API_PERS"
    ): Response<GetAccessTokenResponse>
}