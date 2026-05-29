package com.rvodevelopment.tuinmaat.di

import com.rvodevelopment.tuinmaat.service.DeepLinkHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class KoinHelper : KoinComponent {
    fun getDeepLinkHandler(): DeepLinkHandler = get()
}
