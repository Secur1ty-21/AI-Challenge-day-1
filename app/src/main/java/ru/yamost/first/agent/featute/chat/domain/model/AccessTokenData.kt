package ru.yamost.first.agent.featute.chat.domain.model

class AccessTokenData(
    val token: String,
    val expiredAt: Long
) {
    companion object {
        @JvmStatic
        val EMPTY by lazy { AccessTokenData("", 0L) }
    }
}