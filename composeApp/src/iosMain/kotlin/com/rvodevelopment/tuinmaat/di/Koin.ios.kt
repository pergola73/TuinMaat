package com.rvodevelopment.tuinmaat.di

import com.rvodevelopment.tuinmaat.service.*
import com.rvodevelopment.tuinmaat.data.*
import org.koin.dsl.module
import org.koin.core.module.Module
import org.koin.core.qualifier.named

actual fun platformModule(): Module = module {
    single(named("PLANTNET_API_KEY")) { "" } 
    single(named("GEMINI_API_KEY")) { "" }
    single<PlantDatabase> { getRoomDatabase(getDatabaseBuilder()) }
    single<SharingService> { IosSharingService() }
    single<BiometricService> { IosBiometricService() }
    single<MediaService> { IosMediaService() }
}
