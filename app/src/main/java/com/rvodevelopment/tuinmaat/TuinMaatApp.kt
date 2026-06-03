package com.rvodevelopment.tuinmaat

import android.app.Application
import com.rvodevelopment.tuinmaat.BuildConfig
import com.rvodevelopment.tuinmaat.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TuinMaatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        initKoin(
            plantnetApiKey = BuildConfig.PLANTNET_API_KEY,
            geminiApiKey = BuildConfig.GEMINI_API_KEY,
            revenueCatApiKey = BuildConfig.REVENUECAT_ANDROID_KEY,
        ) {
            androidLogger()
            androidContext(this@TuinMaatApp)
        }
    }
}
