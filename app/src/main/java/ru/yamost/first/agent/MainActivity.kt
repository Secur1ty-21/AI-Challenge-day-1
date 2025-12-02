package ru.yamost.first.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.yamost.first.agent.featute.chat.presentation.ChatScreen
import ru.yamost.first.agent.ui.theme.FirstAgentTheme
import ru.yamost.first.agent.ui.theme.YaColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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