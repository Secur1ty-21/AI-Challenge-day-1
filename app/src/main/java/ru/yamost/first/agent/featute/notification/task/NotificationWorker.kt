package ru.yamost.first.agent.featute.notification.task

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.koin.java.KoinJavaComponent.inject
import ru.yamost.first.agent.R
import ru.yamost.first.agent.core.domain.YaResult
import ru.yamost.first.agent.featute.chat.domain.model.Message
import ru.yamost.first.agent.featute.chat.domain.model.MessageRole
import ru.yamost.first.agent.featute.chat.domain.useCase.GetAnswerUseCase
import java.util.UUID
import java.util.concurrent.TimeUnit

// Worker класс
class NotificationWorker(
    private val applicationContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(applicationContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {

            // Вызов use case
            val getAnswerUseCase = inject<GetAnswerUseCase>(GetAnswerUseCase::class.java).value
            val sessionId = UUID.randomUUID().toString()
            val userMessage = Message(
                text = "Получи список моих задач. Сделай резюме что я должен сделать за сегодня.",
                role = MessageRole.USER,
                timestamp = System.currentTimeMillis(),
                sessionId = sessionId
            )
            when (val result = getAnswerUseCase.execute(
                story = listOf(userMessage),
                temperature = 0f,
                sessionId = sessionId
            )) {
                is YaResult.Success -> {
                    showNotification(result.data.message.text)
                }

                is YaResult.Failure -> {
                    showNotification("Не удалось составить summary по задачам")
                }
            }

            // Показать уведомление

            // Запланировать следующий запуск через 30 секунд
            scheduleNextWork()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun showNotification(message: String) {
        val notificationManager = this@NotificationWorker.applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создание канала для Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Periodic Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification =
            NotificationCompat.Builder(this@NotificationWorker.applicationContext, CHANNEL_ID)
                .setContentTitle("Агент задач")
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                        .setBigContentTitle("Информация по задачам")
                )
                .setSmallIcon(R.drawable.ic_history)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun scheduleNextWork() {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(60, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this@NotificationWorker.applicationContext)
            .enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    companion object {
        private const val CHANNEL_ID = "ru.yamost.first.agent.notification"
        private const val NOTIFICATION_ID = 909090
        const val WORK_NAME = "periodic_notification_work"
    }
}
