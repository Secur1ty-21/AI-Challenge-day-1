package ru.yamost.first.agent.featute.chat.data.network.model

import com.google.gson.annotations.SerializedName
import ru.yamost.first.agent.featute.chat.domain.model.Message

class MessageDto(
    @SerializedName("content")
    val text: String,
    @SerializedName("role")
    val role: String
)

fun MessageDto.mapToDomain(): Message {
    return Message(
        text = text,
        isUser = MessageRole.findByApiLabel(role) == MessageRole.USER
    )
}

fun Message.mapToData(): MessageDto {
    return MessageDto(
        text = text,
        role = if (isUser) {
            MessageRole.USER.apiLabel
        } else {
            MessageRole.ASSISTANT.apiLabel
        }
    )
}