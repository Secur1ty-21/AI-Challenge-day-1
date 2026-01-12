package ru.yamost.first.agent.featute.chat.data.network.model

import com.google.gson.annotations.SerializedName
import ru.yamost.first.agent.featute.chat.domain.model.Answer
import ru.yamost.first.agent.featute.chat.domain.model.McpError
import ru.yamost.first.agent.featute.chat.domain.model.Usage

class GetAnswerResponse(
    @SerializedName("choices")
    val messageList: List<AnswerMessage>,
    @SerializedName("usage")
    val usage: UsageDto
)

class AnswerMessage(
    @SerializedName("message")
    val message: MessageDto,
    @SerializedName("finish_reason")
    val finishReason: String? = null,
)

class UsageDto(
    @SerializedName("prompt_tokens")
    val promptTokens: Long,
    @SerializedName("completion_tokens")
    val completionTokens: Long,
    @SerializedName("precached_prompt_tokens")
    val precachedPromptToken: Long,
    @SerializedName("total_tokens")
    val totalTokens: Long
)

fun GetAnswerResponse.mapToDomain(mcpError: McpError? = null): Answer {
    return Answer(
        message = messageList.first().message.mapToDomain(),
        usage = usage.mapToDomain(),
        mcpError = mcpError
    )
}

fun UsageDto.mapToDomain(): Usage {
    return Usage(
        promptTokens = promptTokens,
        completionTokens = completionTokens,
        precachedTokens = precachedPromptToken,
        totalTokens = totalTokens
    )
}