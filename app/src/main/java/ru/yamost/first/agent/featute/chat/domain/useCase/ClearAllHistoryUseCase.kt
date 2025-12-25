package ru.yamost.first.agent.featute.chat.domain.useCase

import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.domain.api.ChatStorage

class ClearAllHistoryUseCase(
    private val chatStorage: ChatStorage,
    private val getAllDialogsUseCase: GetAllDialogsUseCase
) {
    suspend fun execute(): YaResult<Unit, Unit> {
        val dialogsResult = getAllDialogsUseCase.execute()

        return when (dialogsResult) {
            is YaResult.Success -> {
                dialogsResult.data.forEach { dialog ->
                    chatStorage.deleteDialog(dialog.id)
                }
                YaResult.Success(Unit)
            }
            is YaResult.Failure -> YaResult.Failure(Unit)
        }
    }
}
