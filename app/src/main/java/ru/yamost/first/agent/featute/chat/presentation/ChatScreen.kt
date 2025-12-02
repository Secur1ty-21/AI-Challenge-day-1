package ru.yamost.first.agent.featute.chat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.yamost.first.agent.featute.chat.presentation.model.ChatEvent
import ru.yamost.first.agent.featute.chat.presentation.model.ChatState
import ru.yamost.first.agent.featute.chat.presentation.model.MessageUi

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel(),
    systemPadding: PaddingValues
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val action = viewModel.action.collectAsStateWithLifecycle().value
    ChatScreen(
        state = state,
        modifier = Modifier.padding(systemPadding),
        eventCallback = { viewModel.obtainEvent(it) }
    )
}

@Composable
private fun ChatScreen(
    state: ChatState,
    modifier: Modifier = Modifier,
    eventCallback: (ChatEvent) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true
        ) {
            items(state.story.reversed()) { message ->
                ChatMessageItem(message)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.input,
                onValueChange = { eventCallback(ChatEvent.TypeRequest(it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Введите сообщение") },
                singleLine = false,
                maxLines = 4,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { eventCallback(ChatEvent.BtnSendClick) })
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                IconButton(onClick = { eventCallback(ChatEvent.BtnSendClick) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.Send,
                        contentDescription = "Отправить"
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: MessageUi) {
    val isUserMessage = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUserMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
                color = if (isUserMessage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
