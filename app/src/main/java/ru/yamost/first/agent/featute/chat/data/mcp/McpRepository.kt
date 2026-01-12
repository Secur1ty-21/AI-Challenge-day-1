package ru.yamost.first.agent.featute.chat.data.mcp

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.domain.model.McpError
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class McpRepository {

    private val apiService = McpClient.apiService

    // Инициализация сессии
    suspend fun initialize(): YaResult<InitializeResult, McpError> {
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
                val resultJson =
                    response.body()?.string()?.substringAfter("data:")?.trim().orEmpty()
                Log.d("McpRepository", "json = $resultJson")
                val result = Gson().fromJson(resultJson, InitializeResponse::class.java)
                Log.d("McpRepository", "result = $result")
                Log.d("McpRepository", "Initialized: ${result.result.serverInfo.name}")
                YaResult.Success(result.result)
            } else {
                YaResult.Failure(McpError.InitializationFailed)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            YaResult.Failure(e.toMcpError())
        }
    }

    // Получение списка инструментов (теперь требует инициализации)
    suspend fun getToolsList(): YaResult<List<Tool>, McpError> {
        return try {
            val request = JsonRpcRequest(
                id = 2,
                method = "tools/list",
                params = emptyMap()
            )

            val response = apiService.toolsList(request)

            if (response.isSuccessful && response.body() != null) {
                val resultJson =
                    response.body()?.string()?.substringAfter("data:")?.trim().orEmpty()
                val tools = Gson().fromJson(resultJson, ToolsListResponse::class.java)
                YaResult.Success(tools.result.tools)
            } else {
                YaResult.Failure(McpError.ServerUnavailable)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            YaResult.Failure(e.toMcpError())
        }
    }

    // Вызов инструмента с параметрами
    suspend fun callTool(
        toolName: String,
        arguments: Map<String, Any> = emptyMap()
    ): YaResult<String, McpError> {
        return try {
            val request = JsonRpcRequest(
                id = 3,
                method = "tools/call",
                params = mapOf(
                    "name" to toolName,
                    "arguments" to arguments
                )
            )

            val response = apiService.toolCall(request)

            if (response.isSuccessful && response.body() != null) {
                val result = response.body()?.string().orEmpty()

                if (result.isNotEmpty()) {
                    val gson = Gson()
                    val jsonObject: JsonObject = gson.fromJson(result, JsonObject::class.java).getAsJsonObject("result")
                    YaResult.Success(jsonObject.toString())
                } else {
                    YaResult.Failure(McpError.ToolCallFailed)
                }
            } else {
                YaResult.Failure(McpError.ToolCallFailed)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            YaResult.Failure(e.toMcpError())
        }
    }

    private fun Exception.toMcpError(): McpError {
        return when (this) {
            is UnknownHostException -> McpError.ServerUnavailable
            is ConnectException -> McpError.ConnectionError
            is SocketTimeoutException -> McpError.ConnectionError
            else -> McpError.Unknown(message ?: "Unknown error")
        }
    }
}