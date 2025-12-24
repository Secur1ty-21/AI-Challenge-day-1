package ru.yamost.first.agent.featute.chat.data.mcp

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.yamost.first.agent.BuildConfig
import java.util.concurrent.TimeUnit

object McpClient {
    // Хранение текущего Session ID
    @Volatile
    private var currentSessionId: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json, text/event-stream")

            // Добавляем Session ID если он есть
            currentSessionId?.let { sessionId ->
                requestBuilder.addHeader("mcp-session-id", sessionId)
            }

            val response = chain.proceed(requestBuilder.build())

            // Извлекаем Session ID из ответа если сервер его вернул
            response.header("mcp-session-id")?.let { sessionId ->
                currentSessionId = sessionId
                Log.d("McpClient", "Received Session ID: $sessionId")
            }

            response
        }
        .connectTimeout(240, TimeUnit.SECONDS)
        .readTimeout(240, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.MCP_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: McpApiService = retrofit.create(McpApiService::class.java)

    fun clearSession() {
        currentSessionId = null
    }
}
