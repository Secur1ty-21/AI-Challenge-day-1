package ru.yamost.first.agent.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    protected fun runSafely(
        block: suspend () -> Unit,
        onError: suspend (Throwable) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                block()
            }.onFailure { error ->
                onError(error)
            }
        }
    }
}