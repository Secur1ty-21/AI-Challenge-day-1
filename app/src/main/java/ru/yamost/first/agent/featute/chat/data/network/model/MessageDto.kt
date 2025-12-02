package ru.yamost.first.agent.featute.chat.data.network.model

import com.google.gson.annotations.SerializedName
import ru.yamost.first.agent.featute.chat.domain.model.Message
import ru.yamost.first.agent.featute.chat.domain.model.MessageRole

class MessageDto(
    @SerializedName("content")
    val text: String,
    @SerializedName("role")
    val role: String
)

fun MessageDto.mapToDomain(): Message {
    return Message(
        text = text,
        role = MessageRole.findByApiLabel(role),
        timestamp = System.currentTimeMillis()
    )
}

fun Message.mapToData(): MessageDto {
    return MessageDto(
        text = text,
        role = role.apiLabel
    )
}