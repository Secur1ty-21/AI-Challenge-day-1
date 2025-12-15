package ru.yamost.first.agent.featute.chat.data.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.yamost.first.agent.featute.chat.data.storage.ChatDialogMapper.toDomain
import ru.yamost.first.agent.featute.chat.data.storage.ChatDialogMapper.toDto
import ru.yamost.first.agent.featute.chat.data.storage.model.ChatDialogDto
import ru.yamost.first.agent.featute.chat.data.storage.model.MessageDto
import ru.yamost.first.agent.featute.chat.domain.api.ChatStorage
import ru.yamost.first.agent.featute.chat.domain.model.ChatDialog
import ru.yamost.first.agent.featute.chat.domain.model.Message
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class ChatStorageImpl(
    private val context: Context,
    private val gson: Gson = Gson()
) : ChatStorage {

    companion object {
        private const val DIALOGS_FOLDER = "dialogs"
        private const val MESSAGES_FOLDER = "messages"
        private const val DIALOGS_LIST_FILE = "dialogs_list.json"
        private const val MAX_TITLE_LENGTH = 30
    }

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // Получаем корневую папку приложения
    private val appStorageDir: File
        get() = context.filesDir

    private val dialogsDir: File
        get() = File(appStorageDir, DIALOGS_FOLDER).apply {
            if (!exists()) mkdirs()
        }

    private val messagesDir: File
        get() = File(appStorageDir, MESSAGES_FOLDER).apply {
            if (!exists()) mkdirs()
        }

    private val dialogsListFile: File
        get() = File(dialogsDir, DIALOGS_LIST_FILE)

    override suspend fun saveMessage(message: Message, sessionId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                // 1. Сохраняем сообщение в файл диалога
                val messageWithSession = message.copy(sessionId = sessionId)
                saveMessageToDialogFile(messageWithSession)

                // 2. Обновляем или создаем диалог
                updateOrCreateDialog(messageWithSession, sessionId)
            }
        }

    override suspend fun getAllDialogs(): Result<List<ChatDialog>> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!dialogsListFile.exists()) {
                    return@runCatching emptyList<ChatDialog>()
                }

                val json = dialogsListFile.readText()
                if (json.isBlank()) {
                    return@runCatching emptyList<ChatDialog>()
                }

                val type = object : TypeToken<List<ChatDialogDto>>() {}.type
                val dialogDtos = gson.fromJson<List<ChatDialogDto>>(json, type)
                dialogDtos?.map { it.toDomain() } ?: emptyList()
            }
        }

    override suspend fun getMessagesBySessionId(sessionId: String): Result<List<Message>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val messagesFile = File(messagesDir, "$sessionId.json")
                if (!messagesFile.exists()) {
                    return@runCatching emptyList<Message>()
                }

                val json = messagesFile.readText()
                if (json.isBlank()) {
                    return@runCatching emptyList<Message>()
                }

                val type = object : TypeToken<List<MessageDto>>() {}.type
                val messageDtos = gson.fromJson<List<MessageDto>>(json, type)
                messageDtos?.map { it.toDomain() } ?: emptyList()
            }
        }

    override suspend fun createDialog(title: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val sessionId = UUID.randomUUID().toString()
                val now = LocalDateTime.now()

                val newDialog = ChatDialog(
                    id = sessionId,
                    title = title.take(MAX_TITLE_LENGTH),
                    lastMessage = "Диалог создан",
                    lastMessageTimestamp = now,
                    messageCount = 0
                )

                // Добавляем в список диалогов
                val currentDialogs = getAllDialogs().getOrElse { emptyList() }
                val updatedDialogs = currentDialogs + newDialog

                saveDialogsList(updatedDialogs)

                // Создаем пустой файл для сообщений
                val messagesFile = File(messagesDir, "$sessionId.json")
                messagesFile.writeText("[]")

                sessionId
            }
        }

    override suspend fun deleteDialog(sessionId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Удаляем диалог из списка
                val currentDialogs = getAllDialogs().getOrElse { emptyList() }
                val updatedDialogs = currentDialogs.filter { it.id != sessionId }
                saveDialogsList(updatedDialogs)

                // Удаляем файл сообщений
                val messagesFile = File(messagesDir, "$sessionId.json")
                if (messagesFile.exists()) {
                    messagesFile.delete()
                }
            }
        }

    override suspend fun updateDialog(sessionId: String, title: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val currentDialogs = getAllDialogs().getOrElse { emptyList() }
                val dialogIndex = currentDialogs.indexOfFirst { it.id == sessionId }

                if (dialogIndex != -1) {
                    val dialog = currentDialogs[dialogIndex]
                    val updatedDialog = dialog.copy(title = title.take(MAX_TITLE_LENGTH))
                    val updatedDialogs = currentDialogs.toMutableList().apply {
                        set(dialogIndex, updatedDialog)
                    }
                    saveDialogsList(updatedDialogs)
                }
            }
        }

    // Приватные вспомогательные методы
    private fun saveMessageToDialogFile(message: Message) {
        val messagesFile = File(messagesDir, "${message.sessionId}.json")

        val currentMessages = if (messagesFile.exists()) {
            val json = messagesFile.readText()
            if (json.isBlank()) {
                emptyList<MessageDto>()
            } else {
                val type = object : TypeToken<List<MessageDto>>() {}.type
                gson.fromJson<List<MessageDto>>(json, type) ?: emptyList()
            }
        } else {
            emptyList()
        }

        val updatedMessages = currentMessages + message.toDto()
        val json = gson.toJson(updatedMessages)
        messagesFile.writeText(json)
    }

    private suspend fun updateOrCreateDialog(message: Message, sessionId: String) {
        val currentDialogs = getAllDialogs().getOrElse { emptyList() }

        val dialogIndex = currentDialogs.indexOfFirst { it.id == sessionId }
        val now = LocalDateTime.now()

        val dialogTitle = if (message.text.length > MAX_TITLE_LENGTH) {
            message.text.take(MAX_TITLE_LENGTH - 3) + "..."
        } else {
            message.text
        }

        val updatedDialogs = if (dialogIndex != -1) {
            // Обновляем существующий диалог
            val dialog = currentDialogs[dialogIndex]
            val updatedDialog = dialog.copy(
                lastMessage = message.text,
                lastMessageTimestamp = now,
                messageCount = dialog.messageCount + 1
            )
            currentDialogs.toMutableList().apply {
                set(dialogIndex, updatedDialog)
            }
        } else {
            // Создаем новый диалог
            val newDialog = ChatDialog(
                id = sessionId,
                title = dialogTitle,
                lastMessage = message.text,
                lastMessageTimestamp = now,
                messageCount = 1
            )
            currentDialogs + newDialog
        }

        saveDialogsList(updatedDialogs)
    }

    private fun saveDialogsList(dialogs: List<ChatDialog>) {
        val dialogDtos = dialogs.map { it.toDto() }
        val json = gson.toJson(dialogDtos)
        dialogsListFile.writeText(json)
    }
}