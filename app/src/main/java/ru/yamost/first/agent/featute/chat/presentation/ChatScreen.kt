package ru.yamost.first.agent.featute.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.yamost.first.agent.R
import ru.yamost.first.agent.featute.chat.domain.model.ChatDialog
import ru.yamost.first.agent.featute.chat.domain.model.MessageRole
import ru.yamost.first.agent.featute.chat.presentation.model.ChatEvent
import ru.yamost.first.agent.featute.chat.presentation.model.ChatState
import ru.yamost.first.agent.featute.chat.presentation.model.MessageUi
import ru.yamost.first.agent.ui.theme.YaColor
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel(),
    systemPadding: PaddingValues
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        modifier = Modifier.padding(systemPadding),
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            DrawerContent(
                state = state,
                onDialogSelected = { dialogId ->
                    viewModel.obtainEvent(ChatEvent.SelectDialog(dialogId))
                    scope.launch {
                        drawerState.close()
                        if (state.isMenuOpen) {
                            viewModel.obtainEvent(ChatEvent.ToggleMenuClick)
                        }
                    }
                },
                onClose = {
                    scope.launch {
                        drawerState.close()
                        if (state.isMenuOpen) {
                            viewModel.obtainEvent(ChatEvent.ToggleMenuClick)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    ) {
        ChatContent(
            state = state,
            modifier = Modifier.background(color = YaColor.ChatBackground),
            eventCallback = { viewModel.obtainEvent(it) }
        )
        LaunchedEffect(state.isMenuOpen) {
            if (state.isMenuOpen && drawerState.isClosed) {
                scope.launch { drawerState.open() }
            } else if (state.isMenuOpen.not() && drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    state: ChatState,
    onDialogSelected: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .safeDrawingPadding()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Заголовок меню
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "История диалогов",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрыть меню"
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Список диалогов
        if (state.dialogs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_history),
                    contentDescription = "Нет истории",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Нет сохраненных диалогов",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                reverseLayout = true
            ) {
                items(state.dialogs) { dialog ->
                    DialogItem(
                        dialog = dialog,
                        onClick = { onDialogSelected(dialog.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogItem(
    dialog: ChatDialog,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = dialog.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (dialog.lastMessage.isNotEmpty()) {
                Text(
                    text = dialog.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dialog.lastMessageTimestamp.format(
                        DateTimeFormatter.ofPattern("dd.MM HH:mm", Locale.ENGLISH)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "${dialog.messageCount}",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatContent(
    state: ChatState,
    modifier: Modifier = Modifier,
    eventCallback: (ChatEvent) -> Unit
) {
    Column(
        modifier = modifier
            .background(color = YaColor.ChatBackground)
            .fillMaxSize()
            .imePadding()
            .padding(bottom = 8.dp)
    ) {
        // Верхняя панель с заголовком и кнопкой очистки
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { eventCallback(ChatEvent.ToggleMenuClick) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Меню"
                        )
                    }

                    Text(
                        text = "Giga Chat API",
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = { eventCallback(ChatEvent.BtnClearClick) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_sweep),
                            contentDescription = "Очистить",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Новый диалог",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { eventCallback(ChatEvent.BtnToggleTemperatureClick) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки температуры",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (state.isTemperatureVisible) "Скрыть температуру" else "Показать температуру",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (state.isTemperatureVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            text = "Температура (0.0 - 1.0)",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "0.0") },
                            value = state.temperature,
                            onValueChange = {
                                if (it.isEmpty() || it.toFloatOrNull() != null || it.endsWith('.')) {
                                    eventCallback(ChatEvent.TypeTemperature(it))
                                }
                            },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (state.temperature != "0") {
                                            eventCallback(ChatEvent.TypeTemperature("0"))
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Сбросить",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                state.usage?.let { usage ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                        Text(
                            text = "Использование токенов",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TokenUsageItem(
                                label = "Prompt",
                                value = usage.promptTokens,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TokenUsageItem(
                                label = "Completion",
                                value = usage.completionTokens,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            TokenUsageItem(
                                label = "Precached",
                                value = usage.precachedTokens,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            TokenUsageItem(
                                label = "Total",
                                value = usage.totalTokens,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(state.story.reversed()) { message ->
                MessageBubble(
                    message = message,
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .imePadding()
                .fillMaxWidth()
                .padding(top = 12.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.input,
                onValueChange = { eventCallback(ChatEvent.TypeRequest(it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Введите сообщение...") },
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
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                IconButton(
                    onClick = { eventCallback(ChatEvent.BtnSendClick) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF0088CC),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF666666)
                    ),
                    modifier = Modifier.size(56.dp),
                    enabled = state.input.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.Send,
                        contentDescription = "Отправить",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenUsageItem(
    label: String,
    value: Long,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            text = value.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
fun MessageBubble(message: MessageUi, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = when (message.role) {
            MessageRole.USER -> Arrangement.End
            MessageRole.ASSISTANT -> Arrangement.Start
            MessageRole.SYSTEM -> Arrangement.Center
            MessageRole.FUNCTION -> Arrangement.Center
        },
        modifier = modifier
            .padding(horizontal = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = if (message.role == MessageRole.USER) 20.dp else 8.dp,
                topEnd = if (message.role == MessageRole.USER) 8.dp else 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            color = when (message.role) {
                MessageRole.USER -> YaColor.UserMessageBackground
                MessageRole.ASSISTANT -> YaColor.AssistantMessageBackground
                MessageRole.SYSTEM -> MaterialTheme.colorScheme.surfaceVariant
                MessageRole.FUNCTION -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = Color.White,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.role == MessageRole.USER) {
                        YaColor.UserMessageTextColor
                    } else {
                        YaColor.AssistantMessageTextColor
                    },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (message.role == MessageRole.USER) {
                        YaColor.UserMessageTextColor.copy(alpha = 0.7f)
                    } else {
                        YaColor.AssistantMessageTextColor.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}