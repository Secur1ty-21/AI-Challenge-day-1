package ru.yamost.first.agent.featute.chat.domain.model

class Usage(
    val promptTokens: Long,
    val completionTokens: Long,
    val precachedTokens: Long,
    val totalTokens: Long
)