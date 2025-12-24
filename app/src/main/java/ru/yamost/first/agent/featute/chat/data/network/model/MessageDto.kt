package ru.yamost.first.agent.featute.chat.data.network.model

import com.google.gson.annotations.SerializedName
import ru.yamost.first.agent.featute.chat.domain.model.Message
import ru.yamost.first.agent.featute.chat.domain.model.MessageRole

data class MessageDto(
    @SerializedName("content")
    val text: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("function_call")
    val functionCall: GigaFunctionCall? = null,
    @SerializedName("functions_state_id")
    val functionsStateId: String? = null,
    @SerializedName("name")
    val functionName: String? = null
)

class GigaFunctionCall(
    @SerializedName("name")
    val name: String,
    @SerializedName("arguments")
    val arguments: Map<String, Any>
)

fun MessageDto.mapToDomain(): Message {
    return Message(
        text = text,
        role = MessageRole.findByApiLabel(role),
        timestamp = System.currentTimeMillis(),
        functionStateId = functionsStateId,
        functionName = functionName,
        functionCall = functionCall
    )
}

fun Message.mapToData(): MessageDto {
    return MessageDto(
        text = text,
        role = role.apiLabel,
        functionsStateId = functionStateId,
        functionName = functionName,
        functionCall = functionCall
    )
}