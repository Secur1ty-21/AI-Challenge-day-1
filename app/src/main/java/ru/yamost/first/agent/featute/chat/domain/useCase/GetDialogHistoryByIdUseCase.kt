package ru.yamost.first.agent.featute.chat.domain.useCase

import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.domain.api.ChatStorage
import ru.yamost.first.agent.featute.chat.domain.model.Message

class GetDialogHistoryByIdUseCase(
    private val chatStorage: ChatStorage
) {
    suspend fun execute(id: String): YaResult<List<Message>, Unit> {
        val result = chatStorage.getMessagesBySessionId(id)
        return if (result.isSuccess) {
            YaResult.Success(result.getOrThrow())
        } else {
            YaResult.Failure(Unit)
        }
    }
}