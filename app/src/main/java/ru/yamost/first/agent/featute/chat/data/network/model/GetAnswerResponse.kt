package ru.yamost.first.agent.featute.chat.data.network.model

import com.google.gson.annotations.SerializedName

class GetAnswerResponse(
    @SerializedName("choices")
    val messageList: List<AnswerMessage>
)

class AnswerMessage(
    @SerializedName("message")
    val message: MessageDto
)