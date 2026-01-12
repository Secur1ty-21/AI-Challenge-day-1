package ru.yamost.first.agent.featute.chat.domain.model

sealed interface ChatError {
    data class Mcp(val error: McpError) : ChatError
    data object Network : ChatError
    data object Unknown : ChatError
}
