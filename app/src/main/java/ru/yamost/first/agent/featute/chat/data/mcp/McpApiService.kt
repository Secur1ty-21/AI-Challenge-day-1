package ru.yamost.first.agent.featute.chat.data.mcp

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface McpApiService {

    @POST("sequentialthinking/mcp") // или ваш endpoint для MCP
    suspend fun toolsList(
        @Body request: JsonRpcRequest
    ): Response<ResponseBody>

    @POST("sequentialthinking/mcp")
    suspend fun initialize(
        @Body request: JsonRpcRequest
    ): Response<ResponseBody>

    // Для вызова конкретного инструмента
  /*  @POST("/")
    suspend fun toolCall(
        @Body request: JsonRpcRequest
    ): Response<JsonRpcResponse>*/
}
