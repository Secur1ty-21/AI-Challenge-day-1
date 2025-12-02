package ru.yamost.first.agent.featute.chat.data

import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.data.network.AuthService
import ru.yamost.first.agent.featute.chat.data.network.GptService
import ru.yamost.first.agent.featute.chat.data.network.model.GetAccessTokenResponse
import ru.yamost.first.agent.featute.chat.data.network.model.GetAnswerRequest
import ru.yamost.first.agent.featute.chat.data.network.model.MessageDto
import ru.yamost.first.agent.featute.chat.data.network.model.MessageRole
import ru.yamost.first.agent.featute.chat.data.network.model.mapToData
import ru.yamost.first.agent.featute.chat.data.network.model.mapToDomain
import ru.yamost.first.agent.featute.chat.domain.api.ChatRepository
import ru.yamost.first.agent.featute.chat.domain.model.Message

class CharRepositoryImpl(
    private val authService: AuthService,
    private val gptService: GptService
) : ChatRepository {
    private var expiresAt = 0L
    private var accessToken = ""

    override suspend fun getAnswer(messageList: List<Message>): YaResult<Message, Unit> {
        if (expiresAt < System.currentTimeMillis() - 1000) {
            when (val authResult = auth()) {
                is YaResult.Success -> {
                    val data = authResult.data
                    expiresAt = data.expiredAt.toLong()
                    accessToken = data.accessToken
                }

                is YaResult.Failure -> {
                    return YaResult.Failure(Unit)
                }
            }
        }


        val getAnswerResponse = runCatching {
            gptService.getAnswer(
                bearerToken = "Bearer $accessToken",
                body = GetAnswerRequest(
                    messageList = messageList.map {
                        it.mapToData()
                    }.toMutableList().apply {
                        add(0, MessageDto(
                            text = "Ты преподаватель в техническом университете, студент задает вопрос.",
                            role = MessageRole.SYSTEM.apiLabel
                        ))
                    }
                )
            )
        }.onFailure {
            it.printStackTrace()
        }.getOrNull() ?: return YaResult.Failure(Unit)
        val body = getAnswerResponse.body()

        return if (getAnswerResponse.isSuccessful && body != null) {
            YaResult.Success(body.messageList.first().message.mapToDomain())
        } else {
            YaResult.Failure(Unit)
        }
    }

    private suspend fun auth(): YaResult<GetAccessTokenResponse, Unit> {
        val response = runCatching {
            authService.getAccessToken()
        }.onFailure {
            it.printStackTrace()
        }.getOrNull() ?: return YaResult.Failure(Unit)
        val body = response.body()
        return if (response.isSuccessful && body != null) {
            YaResult.Success(body)
        } else {
            YaResult.Failure(Unit)
        }
    }
}