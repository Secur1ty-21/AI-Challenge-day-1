package ru.yamost.first.agent.featute.chat.presentation.model

data class ChatState(
    val isLoading: Boolean = false,
    val story: List<MessageUi> = emptyList(),
    val input: String = ""
)