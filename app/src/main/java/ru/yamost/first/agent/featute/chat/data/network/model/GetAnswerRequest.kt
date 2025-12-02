package ru.yamost.first.agent.featute.chat.data.network.model

import com.google.gson.annotations.SerializedName

class GetAnswerRequest(
    @SerializedName("model")
    val model: String = AiModelType.LIGHT.apiLabel,
    @SerializedName("stream")
    val isStream: Boolean = false,
    @SerializedName("updateInterval")
    val updateInterval: Int = 0,
    @SerializedName("max_tokens")
    val maxTokens: Int = 100,
    @SerializedName("messages")
    val messageList: List<MessageDto>
)