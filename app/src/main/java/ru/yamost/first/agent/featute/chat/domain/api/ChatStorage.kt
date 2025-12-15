package ru.yamost.first.agent.featute.chat.domain.api

import ru.yamost.first.agent.featute.chat.domain.model.ChatDialog
import ru.yamost.first.agent.featute.chat.domain.model.Message

interface ChatStorage {
    suspend fun saveMessage(message: Message, sessionId: String): Result<Unit>
    suspend fun getAllDialogs(): Result<List<ChatDialog>>
    suspend fun getMessagesBySessionId(sessionId: String): Result<List<Message>>
    suspend fun createDialog(title: String): Result<String>
    suspend fun deleteDialog(sessionId: String): Result<Unit>
    suspend fun updateDialog(sessionId: String, title: String): Result<Unit>
}