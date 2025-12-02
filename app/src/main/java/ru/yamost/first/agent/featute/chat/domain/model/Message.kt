package ru.yamost.first.agent.featute.chat.domain.model

class Message(
    val text: String,
    val role: MessageRole,
    val timestamp: Long
)