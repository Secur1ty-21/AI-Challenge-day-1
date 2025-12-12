package ru.yamost.first.agent.featute.chat.domain.useCase

import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.domain.api.ChatRepository
import ru.yamost.first.agent.featute.chat.domain.model.Answer
import ru.yamost.first.agent.featute.chat.domain.model.Message

class SummaryUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(story: List<Message>): YaResult<Answer, Unit> {
        return chatRepository.summary(story)
    }
}