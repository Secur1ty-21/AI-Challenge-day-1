package ru.yamost.first.agent

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.yamost.first.agent.featute.chat.di.chatModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)
            modules(chatModule)
        }
    }
}