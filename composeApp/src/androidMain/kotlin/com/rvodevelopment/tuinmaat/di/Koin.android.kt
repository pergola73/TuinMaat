package com.rvodevelopment.tuinmaat.di

import com.rvodevelopment.tuinmaat.service.*
import com.rvodevelopment.tuinmaat.data.*
import org.koin.dsl.module
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import com.rvodevelopment.tuinmaat.BuildConfig
import com.rvodevelopment.tuinmaat.util.ActivityProvider

actual fun platformModule(): Module = module {
    single(named("PLANTNET_API_KEY")) { BuildConfig.PLANTNET_API_KEY }
    single(named("GEMINI_API_KEY")) { BuildConfig.GEMINI_API_KEY }
    single<PlantDatabase> { getRoomDatabase(getDatabaseBuilder(get())) }
    single<SharingService> { AndroidSharingService(get()) }
    single<BiometricService> { AndroidBiometricService { ActivityProvider.getCurrentActivity() } }
}
