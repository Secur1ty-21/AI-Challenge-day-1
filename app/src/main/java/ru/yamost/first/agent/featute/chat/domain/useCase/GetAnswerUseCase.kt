package ru.yamost.first.agent.featute.chat.domain.useCase

import android.util.Log
import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.domain.api.ChatRepository
import ru.yamost.first.agent.featute.chat.domain.model.Answer
import ru.yamost.first.agent.featute.chat.domain.model.ChatError
import ru.yamost.first.agent.featute.chat.domain.model.Message
import ru.yamost.first.agent.featute.chat.domain.model.MessageRole

class GetAnswerUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(
        story: List<Message>,
        temperature: Float,
        sessionId: String,
        withRag: Boolean
    ): YaResult<Answer, ChatError> {
        val userMessageCount = story.count { it.role == MessageRole.USER }
        return if (userMessageCount > START_SUMMARY_COUNT) {
            var count = 0
            val lastUserMessages = story.takeLastWhile {
                if (count < START_SUMMARY_COUNT + 2 && it.role == MessageRole.USER) {
                    ++count
                }
                count != START_SUMMARY_COUNT + 2
            }
            Log.v(TAG, "take messages length = ${lastUserMessages.size}")
            when (val summaryResult =
                chatRepository.summary(lastUserMessages.dropLast(1), sessionId)) {
                is YaResult.Success -> {
                    val shortStory = listOf(summaryResult.data.message, lastUserMessages.last())
                    when (val answerResult =
                        chatRepository.getAnswer(shortStory, temperature, sessionId, withRag)) {
                        is YaResult.Success -> YaResult.Success(
                            Answer(
                                message = Message(
                                    text = "# Суммаризация для сжатия контекста\n${summaryResult.data.message.text}" +
                                            "\n\n# Ответ\n${answerResult.data.message.text}",
                                    role = answerResult.data.message.role,
                                    timestamp = answerResult.data.message.timestamp
                                ),
                                usage = answerResult.data.usage,
                                mcpError = summaryResult.data.mcpError ?: answerResult.data.mcpError
                            )
                        )

                        is YaResult.Failure -> answerResult
                    }
                }

                is YaResult.Failure -> summaryResult
            }
        } else {
            chatRepository.getAnswer(story, temperature, sessionId, withRag)
        }
    }

    private companion object {
        const val START_SUMMARY_COUNT = 16
        val TAG = GetAnswerUseCase::class.simpleName ?: ""
    }
}