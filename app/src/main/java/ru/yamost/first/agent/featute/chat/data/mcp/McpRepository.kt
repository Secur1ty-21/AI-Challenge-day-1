package ru.yamost.first.agent.featute.chat.data.mcp

import android.util.Log
import com.google.gson.Gson

class McpRepository {

    private val apiService = McpClient.apiService

    // Инициализация сессии
    suspend fun initialize(): Result<InitializeResult> {
        return try {
            val initParams = InitializeRequest(
                protocolVersion = "2024-11-05",
                capabilities = ClientCapabilities(),
                clientInfo = ClientInfo(
                    name = "Android MCP Client",
                    version = "1.0.0"
                )
            )

            val request = JsonRpcRequest(
                id = 2,
                method = "initialize",
                params = mapOf(
                    "protocolVersion" to initParams.protocolVersion,
                    "capabilities" to initParams.capabilities,
                    "clientInfo" to initParams.clientInfo
                )
            )

            val response = apiService.initialize(request)

            if (response.isSuccessful && response.body() != null) {
                val resultJson = response.body()?.string()?.substringAfter("data:")?.trim().orEmpty()
                Log.d("McpRepository", "json = $resultJson")
                val result = Gson().fromJson(resultJson, InitializeResponse::class.java)
                Log.d("McpRepository", "result = $result")
                Log.d("McpRepository", "Initialized: ${result.result.serverInfo.name}")
                Result.success(result.result)
            } else {
                Result.failure(Exception("Initialize failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Получение списка инструментов (теперь требует инициализации)
    suspend fun getToolsList(): Result<List<Tool>> {
        return try {
            val request = JsonRpcRequest(
                id = 2,
                method = "tools/list",
                params = emptyMap()
            )

            val response = apiService.toolsList(request)

            if (response.isSuccessful && response.body() != null) {
                val resultJson = response.body()?.string()?.substringAfter("data:")?.trim().orEmpty()
                val tools = Gson().fromJson(resultJson, ToolsListResponse::class.java)
                Result.success(tools.result.tools)
            } else {
                Result.failure(Exception("Failed to get tools: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
