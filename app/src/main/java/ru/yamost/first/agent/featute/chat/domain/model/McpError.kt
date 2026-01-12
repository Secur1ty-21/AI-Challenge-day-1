package ru.yamost.first.agent.featute.chat.domain.model

sealed interface McpError {
    data object ConnectionError : McpError
    data object ServerUnavailable : McpError
    data object InitializationFailed : McpError
    data object ToolCallFailed : McpError
    data class Unknown(val message: String) : McpError
}
