package ru.yamost.first.agent.featute.chat.data.mcp

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface McpApiService {

    @POST("mcp")
    suspend fun toolsList(
        @Body request: JsonRpcRequest
    ): Response<ResponseBody>

    @POST("mcp")
    suspend fun initialize(
        @Body request: JsonRpcRequest
    ): Response<ResponseBody>

    @POST("mcp")
    suspend fun toolCall(
        @Body request: JsonRpcRequest
    ): Response<ResponseBody>
}
