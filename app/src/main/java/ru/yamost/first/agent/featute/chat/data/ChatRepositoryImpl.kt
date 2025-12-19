package ru.yamost.first.agent.featute.chat.data

import android.util.Log
import kotlinx.coroutines.delay
import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.data.mcp.McpRepository
import ru.yamost.first.agent.featute.chat.data.mcp.mapToGigaTool
import ru.yamost.first.agent.featute.chat.data.network.AuthService
import ru.yamost.first.agent.featute.chat.data.network.GigaService
import ru.yamost.first.agent.featute.chat.data.network.model.AiModelDto
import ru.yamost.first.agent.featute.chat.data.network.model.FinishReason
import ru.yamost.first.agent.featute.chat.data.network.model.GetAccessTokenResponse
import ru.yamost.first.agent.featute.chat.data.network.model.GetAnswerRequest
import ru.yamost.first.agent.featute.chat.data.network.model.GigaToolDto
import ru.yamost.first.agent.featute.chat.data.network.model.MessageDto
import ru.yamost.first.agent.featute.chat.data.network.model.mapToData
import ru.yamost.first.agent.featute.chat.data.network.model.mapToDomain
import ru.yamost.first.agent.featute.chat.data.storage.Installation
import ru.yamost.first.agent.featute.chat.domain.api.ChatRepository
import ru.yamost.first.agent.featute.chat.domain.api.ChatStorage
import ru.yamost.first.agent.featute.chat.domain.api.TokenRepository
import ru.yamost.first.agent.featute.chat.domain.model.AccessTokenData
import ru.yamost.first.agent.featute.chat.domain.model.Answer
import ru.yamost.first.agent.featute.chat.domain.model.Message
import ru.yamost.first.agent.featute.chat.domain.model.MessageRole
import java.io.File

class ChatRepositoryImpl(
    private val authService: AuthService,
    private val gigaService: GigaService,
    private val tokenRepository: TokenRepository,
    private val appDir: File,
    private val chatStorage: ChatStorage
) : ChatRepository {
    private val mcpRepository = McpRepository()

    override suspend fun getAnswer(
        messageList: List<Message>,
        temperature: Float,
        sessionId: String
    ): YaResult<Answer, Unit> {
        val tokenData = when (val tokenResult = getToken()) {
            is YaResult.Success -> tokenResult.data
            is YaResult.Failure -> return YaResult.Failure(Unit)
        }
        chatStorage.saveMessage(messageList.last(), sessionId)
        val getAnswerResponse = runCatching {
            gigaService.getAnswer(
                bearerToken = BEARER_FORMAT.format(tokenData.token),
                body = GetAnswerRequest(
                    messageList = messageList.map { it.mapToData() }.toMutableList().apply {
                        add(0, MessageDto(
                            text = "Отвечай коротко",
                            role = MessageRole.SYSTEM.apiLabel
                        ))
                    },
                    temperature = temperature,
                    toolList = getToolList()
                ),
                clientId = Installation.id(appDir),
                sessionId = sessionId
            )
        }.onFailure {
            it.printStackTrace()
        }.getOrNull() ?: return YaResult.Failure(Unit)
        val body = getAnswerResponse.body()

        if (getAnswerResponse.isSuccessful && body != null) {
            val answerMessage = body.messageList.firstOrNull()
            val finishReason = answerMessage?.finishReason.orEmpty()
            if (FinishReason.findByApiLabel(finishReason) == FinishReason.FUNCTION_CALL) {
                runCatching {
                    useTool(answerMessage?.message!!)
                }.onSuccess { functionMessage ->
                    val newMessageList = messageList.toMutableList().apply {
                        add(answerMessage!!.message.mapToDomain())
                        add(functionMessage.mapToDomain())
                    }
                    delay(2000)
                    return getAnswer(newMessageList, temperature, sessionId)
                }
            }

            val answer = body.mapToDomain()
            chatStorage.saveMessage(answer.message, sessionId)
            return YaResult.Success(body.mapToDomain())
        } else {
            return YaResult.Failure(Unit)
        }
    }

    override suspend fun summary(messageList: List<Message>, sessionId: String): YaResult<Answer, Unit> {
        val tokenData = when (val tokenResult = getToken()) {
            is YaResult.Success -> tokenResult.data
            is YaResult.Failure -> return YaResult.Failure(Unit)
        }
        val getAnswerResponse = runCatching {
            gigaService.getAnswer(
                bearerToken = BEARER_FORMAT.format(tokenData.token),
                body = GetAnswerRequest(
                    messageList = messageList.map {
                        it.mapToData()
                    }.toMutableList().apply {
                        add(getSummarySystemPrompt())
                    },
                    temperature = 0f,
                    toolList = getToolList()
                ),
                clientId = Installation.id(appDir),
                sessionId = sessionId
            )
        }.onFailure {
            it.printStackTrace()
        }.getOrNull() ?: return YaResult.Failure(Unit)
        val body = getAnswerResponse.body()

        return if (getAnswerResponse.isSuccessful && body != null) {
            YaResult.Success(body.mapToDomain())
        } else {
            YaResult.Failure(Unit)
        }
    }

    private suspend fun getToolList(): List<GigaToolDto> {
        val initialize = mcpRepository.initialize()
        initialize.onSuccess {
            mcpRepository.getToolsList().onSuccess { toolList ->
                if (toolList.isNotEmpty()) {
                    return toolList.map { it.mapToGigaTool() }
                }
            }
        }
        throw IllegalStateException()
    }

    private suspend fun useTool(messageDto: MessageDto): MessageDto {
        val function = messageDto.functionCall!!
        mcpRepository.callTool(
            toolName = function.name,
            arguments = function.arguments
        ).onSuccess { responseMessage ->
            return MessageDto(
                text = responseMessage,
                role = MessageRole.FUNCTION.apiLabel,
                functionsStateId = null,
                functionName = function.name
            )
        }
        throw IllegalStateException()
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

    private fun getSummarySystemPrompt(): MessageDto {
        val task = "Задача: Сделать краткое резюме нашего диалога, используюя только текст моих сообщений и твох ответов." +
                "Ни в коем случае не придумывай факты, которых не было в нашей истории диалога."
        val outputFormat = "Фомат ответа: Короткое резюме нашего диалога."
        return MessageDto(
            text = task + outputFormat,
            role = MessageRole.USER.apiLabel
        )
    }

    private fun getHelperSystemPrompt(): MessageDto {
        val roleState = "Ты учитель по составлению промптов."
        val task = "Задача: Для более качественного ответа помочь составить промт пользователю."
        val outputFormat = "Для дачи конечного ответа тебе обязательно нужно получить от пользователя инфомарцию по всем 5 пунктам:" +
                "1. указана чёткая роль в которой нужно отвечать в сообщениях пользователя." +
                "2. есть детальное описание задачи (> 50 символов) в сообщениях пользователя." +
                "3. есть контекст в сообщениях пользователя." +
                "4. есть примеры в сообщениях пользователя." +
                "5. указан формат ответа в сообщениях пользователя." +
                "Формат ответа: Задай ровно один наводящий вопрос, чтобы получить информацию по одному из пунтов выше." +
                "Опираться на пункты выше в вопросе обязательно." +
                "В конце сообщения добавь номер пункта из списка выше для уточнения которого задаешь вопрос."
        val sample = "Пример. Сообщение пользователя: Помоги выбрать игрушку." +
                "Ответ: В какой роли требуется ответить? 1" +
                "Сообщение пользователя: В роли консультанта" +
                "Ответ: Опишите вашу задачу детальнее, цена, место покупки, категория игрушки? 1 из 5" +
                "Сообщение пользователя: до 1000, детский магазин, машинка." +
                "Ответ: Опишите человека для которого покупаете? 2"

        val message = roleState + task + outputFormat + sample
        Log.v(TAG, "helper prompt lenght = ${message.length}")
        return MessageDto(
            text = message,
            role = MessageRole.SYSTEM.apiLabel
        )
    }

    private fun getCookingSystemPrompt(): MessageDto {
        val roleState = "Ты шеф повар в хорошем ресторане с итальянским акцентом."
        val task = "Задача: Помочь пользователю составить необычное меню на свой праздник." +
         "Постарайся избегать в выборе продуктов аллергенов."
        val outputFormat = "Для дачи конечного ответа тебе обязательно нужно получить от пользователя инфомарцию по всем 4 пунктам:" +
                "1. Кол-во блюд." +
                "2. Кол-во гостей." +
                "3. Кухня мира." +
                "4. Предпочтения гостей." +
                "Формат ответа: Задай ровно один наводящий вопрос, чтобы получить информацию по одному из пунтов выше." +
                "Опираться на пункты выше в вопросе обязательно." +
                "В конце сообщения добавь номер пункта из списка выше для уточнения которого задаешь вопрос." +
                "Если сообщение пользователя никак не относится к готовке, сообщи ему для чего он тут." +
                "Если ответ конечный: Напиши напротив каждого пункта инфомарцию полученную от пользователя"
        val message = roleState + task + outputFormat
        Log.v(TAG, "cooking prompt lenght = ${message.length}")
        return MessageDto(
            text = message,
            role = MessageRole.SYSTEM.apiLabel
        )
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