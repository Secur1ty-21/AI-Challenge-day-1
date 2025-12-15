package ru.yamost.first.agent.featute.chat.data.storage.model

import com.google.gson.annotations.SerializedName

class ChatDialogDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("last_message")
    val lastMessage: String,

    @SerializedName("last_message_timestamp")
    val lastMessageTimestamp: String, // ISO string format

    @SerializedName("message_count")
    val messageCount: Int,

    @SerializedName("created_at")
    val createdAt: String
)