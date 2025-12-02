package ru.yamost.first.agent.featute.chat.data.network.model

import com.google.gson.annotations.SerializedName

data class GetAiModelListResponse (
    @SerializedName("data")
    val list: List<AiModelDto>
)

data class AiModelDto(
    @SerializedName("id")
    val name: String,
    @SerializedName("object")
    val type: String,
    @SerializedName("owned_by")
    val owner: String
)