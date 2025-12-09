package ru.yamost.first.agent.featute.chat.presentation.model

sealed interface ChatEvent {
    class TypeRequest(val text: String) : ChatEvent
    class TypeTemperature(val temperature: String) : ChatEvent
    object BtnSendClick : ChatEvent
    object BtnClearClick : ChatEvent
}