package ru.yamost.first.agent.featute.chat.domain.api

import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.domain.model.Message

interface ChatRepository {
    suspend fun getAnswer(messageList: List<Message>): YaResult<Message, Unit>
}