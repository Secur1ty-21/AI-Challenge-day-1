package ru.yamost.first.agent.featute.chat.domain.model

data class Answer(
    val message: Message,
    val usage: Usage,
    val mcpError: McpError? = null
)