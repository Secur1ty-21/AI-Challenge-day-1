package ru.yamost.first.agent.featute.chat.data.network.model

enum class MessageRole(val apiLabel: String) {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    companion object {
        fun findByApiLabel(label: String): MessageRole {
            return entries.find { it.apiLabel == label } ?:
                throw IllegalStateException()
        }
    }
}