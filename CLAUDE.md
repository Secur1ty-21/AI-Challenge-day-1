# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Building
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean
```

### Testing
```bash
# Run all unit tests
./gradlew test

# Run all instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "ru.yamost.first.agent.YourTestClass"

# Run tests with coverage
./gradlew testDebugUnitTest
```

### Running
```bash
# Install debug build on connected device
./gradlew installDebug

# Uninstall app
./gradlew uninstallDebug
```

### Code Quality
```bash
# Lint check
./gradlew lint

# Generate lint report
./gradlew lintDebug
```

## Configuration

### Required: local.properties
Create `local.properties` file in project root with:
```properties
MODEL_API_KEY=your_gigachat_api_key
MCP_BASE_URL=your_mcp_server_url
```

These values are injected into BuildConfig at compile time.

## Architecture Overview

### Clean Architecture Layers

```
Presentation (UI) ←→ Domain (Business Logic) ←→ Data (Implementation)
```

**Presentation**: Jetpack Compose UI + ViewModel (MVVM pattern)
- `ChatScreen.kt` - Main UI with Compose (Material 3, dark theme)
- `ChatViewModel.kt` - State management with StateFlow
- `ChatState` - Immutable UI state with dialog management, RAG toggle, temperature
- `ChatEvent` - Sealed interface for user actions (16 event types)

**Domain**: Business logic, independent of frameworks
- Use Cases: `GetAnswerUseCase`, `GetAllDialogsUseCase`, `GetDialogHistoryByIdUseCase`, `DeleteDialogUseCase`, `ClearAllHistoryUseCase`, `SummaryUseCase`
- Repository interfaces: `ChatRepository`, `ChatStorage`, `TokenRepository`
- Models: `Message`, `ChatDialog`, `Answer`, `Usage`, `ChatError`, `McpError`
- `MessageRole` enum: SYSTEM, USER, ASSISTANT, FUNCTION

**Data**: Implementation details
- `ChatRepositoryImpl` - Orchestrates GigaChat API + MCP + Storage
- Network: Retrofit services (`GigaService`, `AuthService`)
- MCP: `McpRepository` for Model Context Protocol (JSON-RPC 2.0)
- Storage: `ChatStorageImpl` - File-based JSON storage in `filesDir/messages/`

### Dependency Injection (Koin)

Module: `ChatModule.kt`
- `single` for Retrofit clients, Services, Repositories
- `factory` for Use Cases (new instance per injection)
- `viewModel` for ChatViewModel
- Custom OkHttpClient with Russian Trusted Root CA certificate

**Network Configuration**:
- AuthService base URL: `https://ngw.devices.sberbank.ru:9443/api/v2/`
- GigaService base URL: `https://gigachat.devices.sberbank.ru/api/v1/`
- Timeouts: 30 seconds (connect, read, write)

### Key Patterns

**Event-Driven UI**:
```
User interaction → ChatEvent → ViewModel.obtainEvent() → State update → UI recomposition
```

**Safe Execution** (`BaseViewModel`):
```kotlin
runSafely(
    block = { /* suspend code */ },
    onError = { /* error handling */ }
)
```
All async operations run on `Dispatchers.IO` with exception handling.

**Result Wrapper** (`YaResult`):
```kotlin
sealed interface YaResult<Data, Error> {
    class Success<Data, Error>(val data: Data)
    class Failure<Data, Error>(val error: Error)
}
```

### Critical Flow: Message Processing

1. User sends message → `ChatEvent.BtnSendClick`
2. `GetAnswerUseCase` checks message count:
   - If >16 user messages: triggers `SummaryUseCase` for context compression
3. `ChatRepositoryImpl.getAnswer()`:
   - Fetches/refreshes auth token
   - Initializes MCP connection
   - If RAG enabled: calls `embeddings` tool via MCP
   - Sends request to GigaChat API with tools
4. If `finish_reason == FUNCTION_CALL`:
   - Extracts function name/parameters from response
   - Calls MCP tool via `McpRepository.callTool()`
   - Adds result as FUNCTION role message
   - **Recursively** calls `getAnswer()` with updated history
5. Saves final response to storage
6. ViewModel updates UI state

### MCP (Model Context Protocol) Integration

Located in `data/mcp/`:
- `McpClient` - Singleton HTTP client
- `McpRepository` - JSON-RPC 2.0 operations
- Protocol version: "2024-11-05"

Flow:
1. `initialize()` - Establishes session (stores `mcp-session-id`)
2. `listTools()` - Gets available tools from server
3. `callTool()` - Executes tool with arguments
4. Tools are converted to `GigaToolDto` format for GigaChat API

**Important**: Session ID persists across tool calls via header.

### Context Compression (Summarization)

**Two-level compression**:

1. **UseCase level** (GetAnswerUseCase): Triggered when >16 user messages
   - Takes last 18 messages
   - `SummaryUseCase` creates compressed version via GigaChat
   - Returns combined response with summary and answer

2. **ViewModel level**: After >3 user messages
   - Compresses story to `[first_message, new_assistant_response]`
   - Applied after each successful response
   - Reduces UI state size

### File Storage Structure

```
filesDir/
├── messages/
│   └── {sessionId}.json  // Array of MessageDto
└── dialogs/
    └── dialogs_list.json // Array of ChatDialogDto
```

Each dialog has unique UUID `sessionId`. Messages are appended, dialogs metadata updated on each interaction.

### SSL Certificate Handling

Custom certificate: `res/raw/russian_trusted_root_ca`
- Required for Sberbank GigaChat API
- Loaded in `ChatModule.createOkHttpClient()`
- Creates custom `TrustManager` with X.509 cert

## Important Implementation Notes

### When Adding New Features

1. **New API endpoint**: Add to `GigaService` or `AuthService`
2. **New data model**: Create in `domain/model/`, add DTO in `data/network/model/` or `data/storage/model/`, implement mappers
3. **New business logic**: Create UseCase in `domain/useCase/`, inject in `ChatModule`
4. **New UI event**: Add to `ChatEvent` sealed interface, handle in `ChatViewModel.obtainEvent()`
5. **New UI state**: Add to `ChatState` data class

### Session Management

- `sessionId` is UUID string, generated on first message
- Links all messages in a dialog
- Stored in each `Message.sessionId`
- Used as filename for storage: `{sessionId}.json`

### Function Calling Chain

When AI wants to use a tool:
1. Response has `finish_reason: "function_call"`
2. `functionCall` contains `name` and `arguments` (JSON string)
3. Call tool → get result → add to history with role FUNCTION
4. **Must** re-call `getAnswer()` to get final text response
5. 2-second delay between calls (`delay(2000)`)

### RAG Search

Toggle via `ChatState.isRagChecked` (enabled by default):
- Calls `embeddings` tool with `action: "search"`, `query: {last message}`
- Result appended to last message as `\nRAG-info\n{result}`
- Only affects current request, not saved permanently
- System prompt instructs AI to use RAG info when applicable and cite sources

### Help Command (`/help`)

When user sends `/help`, activates **project assistance mode** with a special system prompt:
- Detects `/help` command in `ChatRepositoryImpl.getAnswer()` (case-insensitive)
- Switches to `getHelpSystemPrompt()` which instructs LLM to use tools for codebase exploration
- RAG search is skipped (tools are called explicitly based on user's question)

**Purpose:** Help user navigate the codebase, find information about architecture, code, history.

**How LLM should respond:**
1. Analyze user's question (code structure, documentation, git history)
2. Call appropriate tool:
   - `embeddings` (action="search", query="...") - for "how it works", "where is", "what does"
   - `git` (command="...", args="...") - for "who changed", "when added", "history", "commits"
3. Return answer based on tool results, not from LLM's own knowledge

### Dialog Management

**UI Features**:
- Dialog history drawer with search functionality
- Message count badges on each dialog
- Select dialog to load history
- Delete individual dialogs with confirmation
- Clear all history with confirmation

**State Fields** (`ChatState`):
- `dialogs: List<ChatDialog>` - all saved dialogs
- `showDeleteConfirmDialog: Boolean` - delete confirmation visibility
- `dialogToDelete: ChatDialog?` - dialog pending deletion
- `showClearAllConfirmDialog: Boolean` - clear all confirmation visibility

**Events** (`ChatEvent`):
- `SelectDialog(dialog)` - load dialog history
- `DeleteDialog(dialog)` - request deletion
- `ConfirmDeleteDialog` / `DismissDeleteDialog` - confirmation actions
- `ClearAllHistory` / `ConfirmClearAllHistory` / `DismissClearAllHistory`

### Temperature Control

- `ChatState.temperature: String = "0"` (stored as string for input validation)
- Validated float input (0.0-1.0)
- Collapsible UI panel in settings
- Reset button to default value
- Event: `TypeTemperature(value)`, `BtnToggleTemperatureClick`

### Token Usage Display

Real-time token consumption metrics shown in UI:
- `promptTokens` - input tokens
- `completionTokens` - generated tokens
- `precachedTokens` - cached tokens
- `totalTokens` - sum of all tokens

Displayed in 4 columns with color coding in `ChatState.usage: Usage?`

### Error Handling

- Network errors return `YaResult.Failure`
- ViewModel shows error in UI via Snackbar
- All suspend functions wrapped in `runSafely` or `runCatching`
- Specific error types: `ChatError`, `McpError` for granular handling

### All ChatEvents

```kotlin
sealed interface ChatEvent {
    data class TypeRequest(val text: String)
    data class TypeTemperature(val text: String)
    data object BtnToggleTemperatureClick
    data object ToggleMenuClick
    data object ToggleRag
    data object BtnSendClick
    data object BtnClearClick
    data class SelectDialog(val dialog: ChatDialog)
    data class DeleteDialog(val dialog: ChatDialog)
    data object ConfirmDeleteDialog
    data object DismissDeleteDialog
    data object ClearAllHistory
    data object ConfirmClearAllHistory
    data object DismissClearAllHistory
}
```

## Package Structure

```
ru.yamost.first.agent/
├── core/
│   ├── domain/YaResult.kt
│   └── presentation/BaseViewModel.kt
├── featute/chat/
│   ├── data/
│   │   ├── network/        // Retrofit services + DTOs
│   │   │   └── model/      // MessageDto, GetAnswerRequest, AiModelType
│   │   ├── storage/        // File-based JSON storage
│   │   │   └── model/      // ChatDialogDto, MessageDto, AccessTokenDataDto
│   │   ├── mcp/            // MCP client + models
│   │   │   ├── McpClient.kt
│   │   │   ├── McpRepository.kt
│   │   │   ├── McpApiService.kt
│   │   │   └── McpModels.kt
│   │   └── ChatRepositoryImpl.kt
│   ├── domain/
│   │   ├── api/            // Repository interfaces
│   │   ├── model/          // Business models + error types
│   │   │   ├── ChatError.kt    // sealed: Mcp, Network, Unknown
│   │   │   └── McpError.kt     // sealed: ConnectionError, ServerUnavailable, etc.
│   │   └── useCase/        // Use cases
│   ├── presentation/
│   │   ├── model/          // UI models (State, Event, Action, MessageUi)
│   │   ├── ChatViewModel.kt
│   │   └── ChatScreen.kt
│   └── di/ChatModule.kt
├── featute/notification/
│   └── task/
│       └── NotificationWorker.kt  // WorkManager for periodic notifications
├── ui/theme/               // Material 3 theme (Color, Theme, Type, YaColor)
├── App.kt                  // Application class with Koin setup
└── MainActivity.kt         // Single activity with ChatScreen
```

## Working with This Codebase

### State Management Pattern

UI state is **immutable**. To update:
```kotlin
_state.update { currentState ->
    currentState.copy(fieldToUpdate = newValue)
}
```

### Adding New MCP Tool

1. Server must expose tool via `tools/list`
2. `McpRepository.callTool(toolName, arguments)` handles invocation
3. If AI should auto-invoke: tool is included in `GetAnswerRequest.toolList`
4. Map MCP tool schema to `GigaToolDto` in `McpModels.mapToGigaTool()`

### Mappers Convention

- `Domain ↔ Data`: `mapToDomain()` / `mapToData()`
- `Domain ↔ UI`: `mapToUi()` / `mapFromUi()`
- Extension functions on source type

### Testing Strategy

- Unit tests: Test Use Cases and ViewModels
- Mocking: Use interfaces for repositories
- UI tests: Compose UI testing with `@Composable` preview

### AI Model Configuration

Available models in `AiModelType`:
- `LIGHT` - "GigaChat-2" (default, fastest)
- `PRO` - "GigaChat-2-Pro" (balanced)
- `MAX` - "GigaChat-2-Max" (most capable)

### Notification Worker

`NotificationWorker.kt` - WorkManager-based periodic task notifications:
- Uses `CoroutineWorker` for async execution
- Calls `GetAnswerUseCase` to summarize tasks via RAG
- Prompt: "Получи список моих задач. Сделай резюме что я должен сделать за сегодня."
- Sends notification with `NotificationCompat.Builder` and `BigTextStyle`
- Channel ID: `ru.yamost.first.agent.notification`
- Self-reschedules every 60 seconds

### Error Types

**ChatError** (sealed interface):
- `Mcp(mcpError: McpError)` - MCP-related errors
- `Network(throwable: Throwable)` - Network failures
- `Unknown(throwable: Throwable)` - Unexpected errors

**McpError** (sealed interface):
- `ConnectionError` - Cannot connect to MCP server
- `ServerUnavailable` - Server not responding
- `InitializationFailed` - Session initialization failed
- `ToolCallFailed` - Tool execution failed
- `Unknown(throwable: Throwable)` - Unexpected MCP errors
