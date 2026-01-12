# FirstAgent - AI Chat приложение на Android

## Описание проекта

Android приложение для взаимодействия с AI моделью GigaChat от Сбербанка. Использует современный стек (Jetpack Compose, Kotlin Coroutines) и интегрирует продвинутые возможности: MCP инструменты и RAG поиск.

## Основной функционал

### Чат с AI
- Отправка и получение сообщений через GigaChat API
- Управление историей диалогов (создание, просмотр, удаление)
- Настройка температуры модели (0.0-1.0)
- Отображение метрик использования токенов (prompt, completion, total)

### RAG (Retrieval-Augmented Generation)
- Семантический поиск по документам через инструмент embeddings
- Переключатель включения/выключения RAG в UI
- Результаты поиска добавляются в контекст запроса

### MCP (Model Context Protocol)
- Интеграция с локальными и удаленными инструментами через JSON-RPC 2.0
- Автоматический вызов инструментов по запросу AI (Function Calling)
- Поддержка цепочек вызовов инструментов

### Управление контекстом
- Автоматическая суммаризация истории при >16 сообщениях
- Сохранение диалогов в файловое хранилище (JSON)
- Очистка всей истории с подтверждением

## Архитектура

**MVVM + Clean Architecture** с тремя слоями:

```
Presentation → Domain ← Data
(UI/ViewModel) (UseCase/Models) (Repository/Network/Storage)
```

### DI
- Koin для dependency injection
- Разделение на модули: network, storage, use cases, viewmodels

### State Management
- StateFlow для реактивного управления состоянием
- Event-driven architecture (ChatEvent → ViewModel → State)

## Ключевые сущности

### Domain Models
- `Message` - сообщение с text, role (SYSTEM/USER/ASSISTANT/FUNCTION), timestamp, sessionId
- `ChatDialog` - диалог с id, title, lastMessage, timestamp, messageCount
- `Answer` - ответ AI с сообщением и usage метриками
- `Usage` - метрики токенов (prompt, completion, precached, total)
- `MessageRole` - роль сообщения (enum)

### UI Models
- `MessageUi` - UI представление сообщения с форматированным временем
- `ChatState` - состояние экрана чата (loading, story, input, temperature, dialogs, etc.)
- `ChatEvent` - события пользователя (sealed interface)

### Network Models
- `GetAnswerRequest` - запрос к GigaChat API
- `GetAnswerResponse` - ответ от GigaChat API
- `GigaToolDto` - описание инструмента для передачи в API
- `MessageDto` - DTO для сообщения с поддержкой function calling

## Внешние сервисы

### GigaChat API (Сбербанк)
- Базовый URL: `https://gigachat.devices.sberbank.ru/api/v1/`
- Эндпоинт: `POST /chat/completions`
- Аутентификация: Bearer token (автообновление)
- Модель: LIGHT (легкая версия)

### MCP Server
- Протокол: JSON-RPC 2.0 over HTTP
- Основные методы: `initialize`, `tools/list`, `tools/call`
- Инструменты: embeddings (поиск), другие по необходимости

## Технический стек

- **UI**: Jetpack Compose, Material 3
- **Network**: Retrofit, OkHttp, Gson
- **DI**: Koin
- **Async**: Kotlin Coroutines, Flow
- **Storage**: File-based JSON (filesDir/messages/)
- **Security**: Custom SSL certificate (Russian Trusted Root CA)

## Структура хранилища

```
app filesDir/
├── dialogs/
│   └── dialogs_list.json - список всех диалогов с метаданными
└── messages/
    └── {sessionId}.json - массив сообщений для каждого диалога
```

## Важные особенности

### Function Calling Flow
1. AI отвечает с `finish_reason == "function_call"`
2. Парсится `functionCall` с именем и параметрами
3. Вызывается инструмент через MCP
4. Результат добавляется в историю как роль FUNCTION
5. Рекурсивно запрашивается финальный ответ

### Context Compression
- При >16 сообщениях пользователя активируется суммаризация
- Берутся последние 18 сообщений
- Создается сжатая версия через `SummaryUseCase`
- Итоговая история: последние 2 сообщения + суммаризация

### Session Management
- UUID sessionId для каждого диалога
- Генерируется при первом сообщении
- Связывает сообщения в единый диалог

## Последние изменения (День 18)

- Добавлена очистка всей истории диалогов с двойным подтверждением
- Добавлено удаление отдельных диалогов с подтверждением
- Улучшен UI бокового меню с иконками и метаданными
- Поддержка edge-to-edge дизайна (Android 14+)
- Запрос разрешения на уведомления (Android 13+)

## Use Cases

- `GetAnswerUseCase` - основной use case для получения ответа от AI с суммаризацией
- `GetAllDialogsUseCase` - получение списка всех диалогов
- `GetDialogHistoryByIdUseCase` - загрузка истории конкретного диалога
- `DeleteDialogUseCase` - удаление диалога
- `ClearAllHistoryUseCase` - очистка всей истории
- `SummaryUseCase` - суммаризация истории сообщений
