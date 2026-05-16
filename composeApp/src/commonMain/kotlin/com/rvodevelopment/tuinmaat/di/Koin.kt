package com.rvodevelopment.tuinmaat.di

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import com.rvodevelopment.tuinmaat.repository.*
import com.rvodevelopment.tuinmaat.service.*
import com.rvodevelopment.tuinmaat.data.*
import com.rvodevelopment.tuinmaat.ui.viewmodel.*
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.core.qualifier.named

fun initKoin(
    useMock: Boolean = false,
    plantnetApiKey: String = "",
    geminiApiKey: String = "",
    appDeclaration: KoinAppDeclaration = {}
) = startKoin {
    appDeclaration()
    modules(commonModule(useMock, plantnetApiKey, geminiApiKey), platformModule())
}

// called by iOS etc
fun doInitKoin(
    useMock: Boolean = false,
    plantnetApiKey: String = "",
    geminiApiKey: String = ""
) = initKoin(
    useMock = useMock,
    plantnetApiKey = plantnetApiKey,
    geminiApiKey = geminiApiKey
) {}

fun commonModule(useMock: Boolean, plantnetApiKey: String, geminiApiKey: String) = module {
    // Gebruik de meegegeven keys of fallback naar wat in platformModule staat (voor Android)
    if (plantnetApiKey.isNotEmpty()) {
        single(named("PLANTNET_API_KEY")) { plantnetApiKey }
    }
    if (geminiApiKey.isNotEmpty()) {
        single(named("GEMINI_API_KEY")) { geminiApiKey }
    }

    single { 
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    single { MessageService() }
    single { DeepLinkHandler(get(), get(), get()) }
    single<TuintipService> { DefaultTuintipService(get()) }
    
    if (useMock) {
        single<AuthService> { MockAuthService() }
        single<StorageService> { MockStorageService() }
    } else {
        single<UserRepository> { FirebaseUserRepository() }
        single<TuinRepository> { FirebaseTuinRepository() }
        single<AuthService> { FirebaseAuthService() }
        single<StorageService> { FirebaseStorageService() }
    }

    single<MediaService> { get() }
    single { get<PlantDatabase>().plantDao() }

    // Centrale plek voor het Gemini model
    single(named("GEMINI_MODEL")) { "gemini-flash-lite-latest" }

    single<AiService> { CommonAiService(
        client = get(),
        plantnetApiKey = get(named("PLANTNET_API_KEY")),
        geminiApiKey = get(named("GEMINI_API_KEY")),
        geminiModel = get(named("GEMINI_MODEL")),
        mediaService = get()
    ) }

    factory { LoginViewModel(get(), get(), get(), get()) }
    factory { HoofdMenuViewModel(get(), get(), get(), get()) }
    factory { PlantenLijstViewModel(get(), get(), get()) }
    factory { (plantId: String?) -> PlantDetailViewModel(get(), get(), plantId) }
    factory { (plantId: String?) -> PlantToevoegenViewModel(get(), get(), get(), get(), get(), get(), get(), plantId) }
    factory { SnoeiKalenderViewModel(get(), get(), get()) }
    factory { InstellingenViewModel(get(), get(), get(), get()) }
}

expect fun platformModule(): Module
