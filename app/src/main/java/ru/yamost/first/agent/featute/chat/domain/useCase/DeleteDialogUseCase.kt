package ru.yamost.first.agent.featute.chat.domain.useCase

import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.domain.api.ChatStorage

class DeleteDialogUseCase(
    private val chatStorage: ChatStorage
) {
    suspend fun execute(dialogId: String): YaResult<Unit, Unit> {
        val result = chatStorage.deleteDialog(dialogId)
        return if (result.isSuccess) {
            YaResult.Success(Unit)
        } else {
            YaResult.Failure(Unit)
        }
    }
}
