package ru.yamost.first.agent.featute.chat.data.mcp

import com.google.gson.annotations.SerializedName

// Базовый запрос JSON-RPC
data class JsonRpcRequest(
    @SerializedName("jsonrpc") val jsonrpc: String = "2.0",
    @SerializedName("id") val id: Int,
    @SerializedName("method") val method: String,
    @SerializedName("params") val params: Map<String, Any> = emptyMap()
)

// Ответ со списком инструментов
data class ToolsListResponse(
    @SerializedName("jsonrpc") val jsonrpc: String,
    @SerializedName("id") val id: Int,
    @SerializedName("result") val result: ToolsResult
)

data class ToolsResult(
    @SerializedName("tools") val tools: List<Tool>,
    @SerializedName("nextCursor") val nextCursor: String?
)

data class Tool(
    @SerializedName("name") val name: String,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String,
    @SerializedName("inputSchema") val inputSchema: Map<String, Any>
)

// Модель ошибки JSON-RPC
data class JsonRpcError(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String
)

data class JsonRpcErrorResponse(
    @SerializedName("jsonrpc") val jsonrpc: String,
    @SerializedName("id") val id: Int,
    @SerializedName("error") val error: JsonRpcError
)

// Запрос инициализации
data class InitializeRequest(
    @SerializedName("protocolVersion") val protocolVersion: String,
    @SerializedName("capabilities") val capabilities: ClientCapabilities,
    @SerializedName("clientInfo") val clientInfo: ClientInfo
)

data class ClientCapabilities(
    @SerializedName("roots") val roots: RootsCapability? = null,
    @SerializedName("sampling") val sampling: Map<String, Any>? = null
)

data class RootsCapability(
    @SerializedName("listChanged") val listChanged: Boolean = false
)

data class ClientInfo(
    @SerializedName("name") val name: String,
    @SerializedName("version") val version: String
)

// Ответ инициализации
data class InitializeResponse(
    @SerializedName("jsonrpc") val jsonrpc: String,
    @SerializedName("id") val id: Int,
    @SerializedName("result") val result: InitializeResult
)

data class InitializeResult(
    @SerializedName("protocolVersion") val protocolVersion: String,
    @SerializedName("capabilities") val capabilities: ServerCapabilities,
    @SerializedName("serverInfo") val serverInfo: ServerInfo
)

data class ServerCapabilities(
    @SerializedName("tools") val tools: Map<String, Any>? = null,
    @SerializedName("resources") val resources: Map<String, Any>? = null,
    @SerializedName("prompts") val prompts: Map<String, Any>? = null
)

data class ServerInfo(
    @SerializedName("name") val name: String,
    @SerializedName("version") val version: String
)

