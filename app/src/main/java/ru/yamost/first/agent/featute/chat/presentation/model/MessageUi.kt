package ru.yamost.first.agent.featute.chat.presentation.model

import ru.yamost.first.agent.featute.chat.domain.model.MessageRole

data class MessageUi(
    val text: String,
    val role: MessageRole,
    val timestamp: String
)