package ru.yamost.first.agent

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ru.yamost.first.agent.featute.chat.presentation.ChatScreen
import ru.yamost.first.agent.featute.notification.task.NotificationWorker
import ru.yamost.first.agent.ui.theme.FirstAgentTheme
import ru.yamost.first.agent.ui.theme.YaColor
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startPeriodicNotifications()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        startPeriodicNotifications()
        setContent {
            FirstAgentTheme {
                Scaffold(
                    modifier = Modifier.background(color = YaColor.ChatBackground).fillMaxSize()
                ) { innerPadding ->
                    ChatScreen(
                        systemPadding = innerPadding
                    )
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startPeriodicNotifications()
                }
                else -> {
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        } else {
            // Для Android 12 и ниже разрешение не требуется
            startPeriodicNotifications()
        }
    }

    private fun startPeriodicNotifications() {
        /*val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                NotificationWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )*/
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FirstAgentTheme {
        Greeting("Android")
    }
}