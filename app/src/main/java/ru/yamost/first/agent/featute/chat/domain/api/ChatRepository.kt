package ru.yamost.first.agent.featute.chat.domain.api

import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.domain.model.Answer
import ru.yamost.first.agent.featute.chat.domain.model.Message

interface ChatRepository {
    suspend fun getAnswer(messageList: List<Message>, temperature: Float, sessionId: String): YaResult<Answer, Unit>
    suspend fun summary(messageList: List<Message>, sessionId: String): YaResult<Answer, Unit>
}