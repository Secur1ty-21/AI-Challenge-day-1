package ru.yamost.first.agent.featute.chat.data.network.model

import com.google.gson.annotations.SerializedName

class GetAccessTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_at")
    val expiredAt: String
)