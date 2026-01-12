package ru.yamost.first.agent.featute.chat.presentation.model

import androidx.annotation.StringRes

sealed interface ChatAction {
    data class ShowError(
        @StringRes val messageResId: Int,
        val formatArg: String? = null
    ) : ChatAction
}