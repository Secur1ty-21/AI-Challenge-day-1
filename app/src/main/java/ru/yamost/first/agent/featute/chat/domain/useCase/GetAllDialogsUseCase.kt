package ru.yamost.first.agent.featute.chat.domain.useCase

import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.domain.api.ChatStorage
import ru.yamost.first.agent.featute.chat.domain.model.ChatDialog

class GetAllDialogsUseCase(
    private val chatStorage: ChatStorage
) {
    suspend fun execute(): YaResult<List<ChatDialog>, Unit> {
        val result = chatStorage.getAllDialogs()
        return if (result.isSuccess) {
            YaResult.Success(result.getOrThrow())
        } else {
            YaResult.Failure(Unit)
        }
    }
}