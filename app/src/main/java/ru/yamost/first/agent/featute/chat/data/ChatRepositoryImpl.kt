package ru.yamost.first.agent.featute.chat.data

import android.util.Log
import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.data.network.AuthService
import ru.yamost.first.agent.featute.chat.data.network.GigaService
import ru.yamost.first.agent.featute.chat.data.network.model.AiModelDto
import ru.yamost.first.agent.featute.chat.data.network.model.GetAccessTokenResponse
import ru.yamost.first.agent.featute.chat.data.network.model.GetAnswerRequest
import ru.yamost.first.agent.featute.chat.data.network.model.MessageDto
import ru.yamost.first.agent.featute.chat.domain.model.MessageRole
import ru.yamost.first.agent.featute.chat.data.network.model.mapToData
import ru.yamost.first.agent.featute.chat.data.network.model.mapToDomain
import ru.yamost.first.agent.featute.chat.data.storage.Installation
import ru.yamost.first.agent.featute.chat.domain.api.ChatRepository
import ru.yamost.first.agent.featute.chat.domain.api.TokenRepository
import ru.yamost.first.agent.featute.chat.domain.model.AccessTokenData
import ru.yamost.first.agent.featute.chat.domain.model.Message
import java.io.File
import java.util.UUID

class ChatRepositoryImpl(
    private val authService: AuthService,
    private val gigaService: GigaService,
    private val tokenRepository: TokenRepository,
    private val appDir: File
) : ChatRepository {
    private val sessionId = UUID.randomUUID().toString()

    override suspend fun getAnswer(messageList: List<Message>): YaResult<Message, Unit> {
        val tokenData = when (val tokenResult = getToken()) {
            is YaResult.Success -> tokenResult.data
            is YaResult.Failure -> return YaResult.Failure(Unit)
        }
        val aiModelList = when (val getModelsResult = sendAiModelListRequest(tokenData.token)) {
            is YaResult.Success -> getModelsResult.data
            is YaResult.Failure -> return YaResult.Failure(Unit)
        }
        for (model in aiModelList) {
            Log.v(TAG, "ModelName = ${model.name}")
        }
        val getAnswerResponse = runCatching {
            gigaService.getAnswer(
                bearerToken = BEARER_FORMAT.format(tokenData.token),
                body = GetAnswerRequest(
                    messageList = messageList.map {
                        it.mapToData()
                    }.toMutableList().apply {
                        add(0, getOutputSystemPrompt())
                    }
                ),
                clientId = Installation.id(appDir),
                sessionId = sessionId
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

    private suspend fun getToken(): YaResult<AccessTokenData, Unit> {
        val savedToken = tokenRepository.getAccessToken()
        return if (savedToken.token.isEmpty() || savedToken.expiredAt < System.currentTimeMillis() - 1000) {
            when (val authResult = auth()) {
                is YaResult.Success -> {
                    val data = authResult.data
                    val newSavedToken = tokenRepository.saveAccessToken(
                        token = data.accessToken,
                        expiredAt = data.expiredAt.toLong()
                    )
                    YaResult.Success(newSavedToken)
                }

                is YaResult.Failure -> {
                    YaResult.Failure(Unit)
                }
            }
        } else {
            YaResult.Success(savedToken)
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

    private suspend fun sendAiModelListRequest(token: String): YaResult<List<AiModelDto>, Unit> {
        val response = runCatching {
            gigaService.getModelList(BEARER_FORMAT.format(token))
        }.onFailure { error ->
            Log.e(TAG, "error in get ai model list", error)
        }.getOrNull() ?: return YaResult.Failure(Unit)
        val body = response.body()
        return if (response.isSuccessful && body != null) {
            YaResult.Success(body.list)
        } else {
            YaResult.Failure(Unit)
        }
    }

    private fun getOutputSystemPrompt(): MessageDto {
        val roleState = "Ты профессиональный инвестиционный консультант."
        val task = "Твоя задача помочь собрать портфель пользователю по его готовности рисковать" +
            " бюджету и горизонту планирования в процетном соотношении по инвестиционным инструментам."
        val formatAnswer = "1. Если пользователь пришел не по вопросу портфеля, нужно направить его в сторону твоих компетенций." +
                "2. Если информации для составления портфеля от пользователя недостаточно, нужно задать наводящие вопросы которые помогут" +
                " повысить точность финального ответа." +
                "3. Если точность определения портфеля по ответам пользователя > 95%, то можно выдавать финальный ответ пользователю." +
                "4. На каждом шаге уточнения, пиши в ответ какая на текущем этапе точность определения соотношения в процентах."
        val limit = "Максимальное количесво ответов пользователя на вопросы - 5." +
                "Если количество ответов превысило 5, то необходимо выдать финальный с " +
                "процентным соотношением по инвестиционным инструментам в портфеле."
        val formatFinalAnswer = "Если от пользователя получено достаточно информации и точность опредления потрфеля > 95%, " +
                "в ответ должно прийти процентное соотношение по инвестиционным " +
                "интсрументам в портфеле. В финальном ответе должно быть только процентное соотношение и ничего более."
        val prompt = roleState + task + formatAnswer + limit + formatFinalAnswer
        return MessageDto(
            text = prompt,
            role = MessageRole.SYSTEM.apiLabel
        )
    }

    private companion object {
        const val BEARER_FORMAT = "Bearer %s"
        val TAG = ChatRepository::class.simpleName ?: ""
    }
}