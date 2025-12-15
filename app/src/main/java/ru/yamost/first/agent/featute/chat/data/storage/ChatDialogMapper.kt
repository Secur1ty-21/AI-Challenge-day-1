package ru.yamost.first.agent.featute.chat.data.storage

import ru.yamost.first.agent.featute.chat.data.storage.model.ChatDialogDto
import ru.yamost.first.agent.featute.chat.data.storage.model.MessageDto
import ru.yamost.first.agent.featute.chat.domain.model.ChatDialog
import ru.yamost.first.agent.featute.chat.domain.model.Message
import ru.yamost.first.agent.featute.chat.domain.model.MessageRole
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ChatDialogMapper {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun ChatDialog.toDto(): ChatDialogDto {
        return ChatDialogDto(
            id = id,
            title = title,
            lastMessage = lastMessage,
            lastMessageTimestamp = lastMessageTimestamp.format(formatter),
            messageCount = messageCount,
            createdAt = lastMessageTimestamp.format(formatter)
        )
    }

    fun ChatDialogDto.toDomain(): ChatDialog {
        return ChatDialog(
            id = id,
            title = title,
            lastMessage = lastMessage,
            lastMessageTimestamp = LocalDateTime.parse(lastMessageTimestamp, formatter),
            messageCount = messageCount
        )
    }

    fun Message.toDto(): MessageDto {
        return MessageDto(
            text = text,
            role = role.name,
            timestamp = timestamp,
            sessionId = sessionId
        )
    }

    fun MessageDto.toDomain(): Message {
        return Message(
            text = text,
            role = MessageRole.valueOf(role),
            timestamp = timestamp,
            sessionId = sessionId
        )
    }
}