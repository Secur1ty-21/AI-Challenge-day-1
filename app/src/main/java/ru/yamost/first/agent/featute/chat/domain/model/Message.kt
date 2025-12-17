package ru.yamost.first.agent.featute.chat.domain.model

import ru.yamost.first.agent.featute.chat.data.network.model.GigaFunctionCall

data class Message(
    val text: String,
    val role: MessageRole,
    val timestamp: Long,
    val functionStateId: String? = null,
    val functionName: String? = null,
    val functionCall: GigaFunctionCall? = null,
    val sessionId: String = ""
)