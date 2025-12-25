package ru.yamost.first.agent.featute.chat.di

import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.yamost.first.agent.BuildConfig
import ru.yamost.first.agent.R
import ru.yamost.first.agent.featute.chat.data.ChatRepositoryImpl
import ru.yamost.first.agent.featute.chat.data.network.AuthService
import ru.yamost.first.agent.featute.chat.data.network.GigaService
import ru.yamost.first.agent.featute.chat.data.storage.ChatStorageImpl
import ru.yamost.first.agent.featute.chat.data.storage.TokenRepositoryImpl
import ru.yamost.first.agent.featute.chat.domain.api.ChatRepository
import ru.yamost.first.agent.featute.chat.domain.api.ChatStorage
import ru.yamost.first.agent.featute.chat.domain.api.TokenRepository
import ru.yamost.first.agent.featute.chat.domain.useCase.ClearAllHistoryUseCase
import ru.yamost.first.agent.featute.chat.domain.useCase.DeleteDialogUseCase
import ru.yamost.first.agent.featute.chat.domain.useCase.GetAllDialogsUseCase
import ru.yamost.first.agent.featute.chat.domain.useCase.GetAnswerUseCase
import ru.yamost.first.agent.featute.chat.domain.useCase.GetDialogHistoryByIdUseCase
import ru.yamost.first.agent.featute.chat.presentation.ChatViewModel
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

fun createOkHttpClient(context: Context): OkHttpClient.Builder {
    val certificateFactory = CertificateFactory.getInstance("X.509")
    val inputStream = context.resources.openRawResource(R.raw.russian_trusted_root_ca)
    val certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate
    inputStream.close()

    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
        load(null, null)
        setCertificateEntry("ca", certificate)
    }

    val trustManagerFactory = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm()
    ).apply {
        init(keyStore)
    }

    val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, trustManagerFactory.trustManagers, null)
    }

    return OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustManagerFactory.trustManagers[0] as X509TrustManager)
}


val chatModule = module {

    single<AuthService> {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        val client = createOkHttpClient(androidContext())
            .addInterceptor(loggingInterceptor)
            .build()
        Retrofit.Builder()
            .baseUrl("https://ngw.devices.sberbank.ru:9443/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(AuthService::class.java)
    }

    single<GigaService> {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        val client = createOkHttpClient(androidContext())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
        Retrofit.Builder()
            .baseUrl("https://gigachat.devices.sberbank.ru/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GigaService::class.java)
    }

    single {
        Gson()
    }

    single<TokenRepository> {
        TokenRepositoryImpl(
            appDir = androidContext().dataDir,
            gson = get()
        )
    }

    single<ChatStorage> {
        ChatStorageImpl(context = androidContext(), gson = get())
    }

    single<ChatRepository> {
        ChatRepositoryImpl(
            authService = get(),
            gigaService = get(),
            tokenRepository = get(),
            appDir = androidContext().filesDir,
            chatStorage = get()
        )
    }

    factory {
        GetAnswerUseCase(
            chatRepository = get()
        )
    }

    factory {
        GetAllDialogsUseCase(chatStorage = get())
    }

    factory {
        GetDialogHistoryByIdUseCase(chatStorage = get())
    }

    factory {
        DeleteDialogUseCase(chatStorage = get())
    }

    factory {
        ClearAllHistoryUseCase(
            chatStorage = get(),
            getAllDialogsUseCase = get()
        )
    }

    viewModel {
        ChatViewModel(
            getAnswerUseCase = get(),
            getAllDialogsUseCase = get(),
            getDialogHistoryByIdUseCase = get(),
            deleteDialogUseCase = get(),
            clearAllHistoryUseCase = get()
        )
    }
}