package ru.yamost.first.agent.featute.chat.data.network.model

enum class FinishReason(val apiLabel: String) {
    FUNCTION_CALL("function_call"),
    UNKNOWN("");

    companion object {
        @JvmStatic
        fun findByApiLabel(apiLabel: String): FinishReason {
            return entries.find { it.apiLabel == apiLabel } ?: UNKNOWN
        }
    }
}