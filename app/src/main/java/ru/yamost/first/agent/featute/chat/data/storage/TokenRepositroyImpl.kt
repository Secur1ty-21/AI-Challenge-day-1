package ru.yamost.first.agent.featute.chat.data.storage

import android.util.Log
import com.google.gson.Gson
import ru.yamost.first.agent.featute.chat.data.storage.model.AccessTokenDataDto
import ru.yamost.first.agent.featute.chat.data.storage.model.mapToDomain
import ru.yamost.first.agent.featute.chat.domain.api.TokenRepository
import ru.yamost.first.agent.featute.chat.domain.model.AccessTokenData
import java.io.File


class TokenRepositoryImpl(
    appDir: File,
    private val gson: Gson
) : TokenRepository {
    private val authFile = File(appDir, TOKEN_FILE_NAME)

    override fun saveAccessToken(token: String, expiredAt: Long): AccessTokenData {
        return runCatching {
            val data = AccessTokenDataDto(token, expiredAt)
            val json = gson.toJson(data)
            if (authFile.exists().not()) {
                authFile.createNewFile()
            }
            authFile.outputStream().use {
                it.write(json.encodeToByteArray())
            }
            getAccessToken()
        }.onFailure { error ->
            Log.e(TAG, "error in save access token", error)
        }.getOrDefault(AccessTokenData.EMPTY)
    }

    override fun getAccessToken(): AccessTokenData {
        return runCatching {
            val fileContent = authFile.bufferedReader().use { it.readText() }
            val data = gson.fromJson(fileContent, AccessTokenDataDto::class.java)
            data.mapToDomain()
        }.onFailure { error ->
            Log.e(TAG, "error in get access token", error)
        }.getOrDefault(AccessTokenData.EMPTY)
    }

    private companion object {
        const val TOKEN_FILE_NAME = "access_token.json"
        val TAG = TokenRepository::class.simpleName ?: ""
    }
}