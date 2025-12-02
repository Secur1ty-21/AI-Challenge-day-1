package ru.yamost.first.agent.featute.chat.data.storage.model

import com.google.gson.annotations.SerializedName
import ru.yamost.first.agent.featute.chat.domain.model.AccessTokenData

class AccessTokenDataDto(
    @SerializedName("token")
    val token: String,
    @SerializedName("expiredAt")
    val expiredAt: Long
)

fun AccessTokenData.mapToData(): AccessTokenDataDto {
    return AccessTokenDataDto(
        token = token,
        expiredAt = expiredAt
    )
}

fun AccessTokenDataDto.mapToDomain(): AccessTokenData {
    return AccessTokenData(
        token = token,
        expiredAt = expiredAt
    )
}