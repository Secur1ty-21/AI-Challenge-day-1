package ru.yamost.first.agent.featute.chat.presentation.model

import ru.yamost.first.agent.featute.chat.domain.model.ChatDialog
import ru.yamost.first.agent.featute.chat.domain.model.Usage

data class ChatState(
    val isLoading: Boolean = false,
    val story: List<MessageUi> = emptyList(),
    val input: String = "",
    val temperature: String = "0",
    val usage: Usage? = null,
    val isTemperatureVisible: Boolean = false,
    val isMenuOpen: Boolean = false,
    val isRagChecked: Boolean = true,
    val dialogs: List<ChatDialog> = emptyList(),
    val showDeleteConfirmDialog: Boolean = false,
    val dialogToDelete: String? = null,
    val showClearAllConfirmDialog: Boolean = false
)