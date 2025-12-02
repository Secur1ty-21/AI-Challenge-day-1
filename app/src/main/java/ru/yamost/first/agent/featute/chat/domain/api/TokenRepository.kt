package ru.yamost.first.agent.featute.chat.domain.api

import ru.yamost.first.agent.featute.chat.domain.model.AccessTokenData

interface TokenRepository {
    fun saveAccessToken(token: String, expiredAt: Long): AccessTokenData
    fun getAccessToken(): AccessTokenData
}