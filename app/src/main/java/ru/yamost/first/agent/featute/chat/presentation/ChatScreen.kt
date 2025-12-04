package ru.yamost.first.agent.featute.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.yamost.first.agent.featute.chat.domain.model.MessageRole
import ru.yamost.first.agent.featute.chat.presentation.model.ChatEvent
import ru.yamost.first.agent.featute.chat.presentation.model.ChatState
import ru.yamost.first.agent.featute.chat.presentation.model.MessageUi
import ru.yamost.first.agent.ui.theme.YaColor

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel(),
    systemPadding: PaddingValues
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val action = viewModel.action.collectAsStateWithLifecycle().value
    ChatScreen(
        state = state,
        modifier = Modifier.background(color = YaColor.ChatBackground).padding(systemPadding),
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
        modifier = modifier.background(color = YaColor.ChatBackground).fillMaxSize()
            .padding(top = 20.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.padding(top = 20.dp).weight(1f).fillMaxWidth(),
            reverseLayout = true
        ) {
            items(state.story.reversed()) { message ->
                MessageBubble(
                    message = message,
                    modifier = Modifier.animateItem().fillMaxWidth()
                )
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
                keyboardActions = KeyboardActions(onSend = { eventCallback(ChatEvent.BtnSendClick) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF37414A),
                    unfocusedContainerColor = Color(0xFF37414A),
                    focusedTextColor = Color(0xFFFFFFFF),
                    unfocusedTextColor = Color(0xFFE0E0E0),
                    cursorColor = Color(0xFF0088CC),
                    focusedLabelColor = Color(0xFF0088CC),
                    unfocusedLabelColor = Color(0xFFCCCCCC),
                    focusedBorderColor = Color(0xFF0088CC),
                    unfocusedBorderColor = Color(0xFF2B2D31),
                    errorBorderColor = Color(0xFFD56B70),
                    unfocusedPlaceholderColor = Color(0xFF78909C),
                    focusedPlaceholderColor = Color(0xFF9E9E9E)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (state.isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                IconButton(
                    onClick = { eventCallback(ChatEvent.BtnSendClick) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF0088CC))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.Send,
                        contentDescription = "Отправить",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: MessageUi, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = when (message.role) {
            MessageRole.USER -> Arrangement.End
            MessageRole.ASSISTANT -> Arrangement.Start
            MessageRole.SYSTEM -> Arrangement.Center
        },
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(
                start = if (message.role == MessageRole.USER) 20.dp else 0.dp,
                end = if (message.role == MessageRole.USER) 0.dp else 20.dp,
            )
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = if (message.role == MessageRole.USER) 16.dp else 0.dp,
                topEnd = if (message.role == MessageRole.USER) 0.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = when (message.role) {
                MessageRole.USER -> YaColor.UserMessageBackground
                MessageRole.ASSISTANT -> YaColor.AssistantMessageBackground
                MessageRole.SYSTEM -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = Color.White,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.role == MessageRole.USER) {
                        YaColor.UserMessageTextColor
                    } else {
                        YaColor.AssistantMessageTextColor
                    }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    color = if (message.role == MessageRole.USER) {
                        YaColor.UserMessageTextColor
                    } else {
                        YaColor.AssistantMessageTextColor
                    }
                )
            }
        }
    }
}