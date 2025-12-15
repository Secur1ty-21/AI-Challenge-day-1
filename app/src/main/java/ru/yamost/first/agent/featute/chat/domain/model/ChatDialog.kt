package ru.yamost.first.agent.featute.chat.domain.model

import java.time.LocalDateTime

data class ChatDialog(
    val id: String,
    val title: String,
    val lastMessage: String,
    val lastMessageTimestamp: LocalDateTime,
    val messageCount: Int
)