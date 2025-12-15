package ru.yamost.first.agent.featute.chat.data.storage.model

import com.google.gson.annotations.SerializedName

class MessageDto(
    @SerializedName("text")
    val text: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("session_id")
    val sessionId: String,

    @SerializedName("message_id")
    val messageId: String = System.currentTimeMillis().toString()
)