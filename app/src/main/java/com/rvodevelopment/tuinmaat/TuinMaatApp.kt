package com.rvodevelopment.tuinmaat

import android.app.Application
import com.rvodevelopment.tuinmaat.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TuinMaatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@TuinMaatApp)
        }
    }
}
