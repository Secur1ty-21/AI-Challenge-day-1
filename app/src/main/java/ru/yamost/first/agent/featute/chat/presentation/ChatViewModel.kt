package ru.yamost.first.agent.featute.chat.presentation

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.core.presentation.BaseViewModel
import ru.yamost.first.agent.featute.chat.domain.model.Message
import ru.yamost.first.agent.featute.chat.domain.model.MessageRole
import ru.yamost.first.agent.featute.chat.domain.useCase.GetAnswerUseCase
import ru.yamost.first.agent.featute.chat.presentation.model.ChatAction
import ru.yamost.first.agent.featute.chat.presentation.model.ChatEvent
import ru.yamost.first.agent.featute.chat.presentation.model.ChatState
import ru.yamost.first.agent.featute.chat.presentation.model.MessageUi
import java.text.SimpleDateFormat
import java.util.Date

class ChatViewModel(
    private val getAnswerUseCase: GetAnswerUseCase
) : BaseViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()
    private val _action = MutableStateFlow<ChatAction?>(null)
    val action = _action.asStateFlow()

    fun obtainEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.TypeRequest -> {
                _state.update {
                    it.copy(
                        input = event.text
                    )
                }
            }

            is ChatEvent.BtnSendClick -> {
                runSafely(
                    block = {
                        val message = MessageUi(
                            text = _state.value.input,
                            role = MessageRole.USER,
                            timestamp = sdf.format(System.currentTimeMillis())
                        )
                         val newStory = _state.value.story.toMutableList().apply {
                             add(message)
                         }
                        _state.update {
                            it.copy(
                                isLoading = true,
                                story = newStory,
                                input = ""
                            )
                        }
                        val answerResult = getAnswerUseCase.execute(
                            story = newStory.map { it.mapToDomain() }
                        )
                        when (answerResult) {
                            is YaResult.Success -> {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        story = newStory.toMutableList().apply {
                                            add(answerResult.data.mapToUi())
                                        }
                                    )
                                }
                            }

                            is YaResult.Failure -> {
                                _state.update {
                                    it.copy(
                                        isLoading = false
                                    )
                                }
                                Log.v(TAG, "failure get answer result")
                            }
                        }
                    },
                    onError = { error ->
                        Log.e(TAG, "error in obtain btn send click", error)
                        _state.update {
                            it.copy(
                                isLoading = false
                            )
                        }
                    }
                )
            }
        }
    }

    private val sdf = SimpleDateFormat("dd.MM HH:mm")

    private fun Message.mapToUi(): MessageUi {
        return MessageUi(
            text = text,
            role = role,
            timestamp = sdf.format(Date(timestamp))
        )
    }

    private fun MessageUi.mapToDomain(): Message {
        return Message(
            text = text,
            role = role,
            timestamp = sdf.parse(timestamp)?.time ?: System.currentTimeMillis()
        )
    }

    private companion object {
        val TAG = ChatViewModel::class.simpleName ?: ""
    }
}