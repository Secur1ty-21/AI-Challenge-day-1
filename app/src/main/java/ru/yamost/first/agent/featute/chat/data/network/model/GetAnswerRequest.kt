package ru.yamost.first.agent.featute.chat.data.network.model

import com.google.gson.annotations.SerializedName

class GetAnswerRequest(
    @SerializedName("model")
    val model: String = "GigaChat-2-Max",
    @SerializedName("stream")
    val isStream: Boolean = false,
    @SerializedName("updateInterval")
    val updateInterval: Int = 0,
    @SerializedName("max_tokens")
    val maxTokens: Int = 500,
    @SerializedName("messages")
    val messageList: List<MessageDto>
)