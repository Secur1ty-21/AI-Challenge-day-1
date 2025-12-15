package ru.yamost.first.agent.featute.chat.domain.model

data class Message(
    val text: String,
    val role: MessageRole,
    val timestamp: Long,
    val sessionId: String = ""
)