package com.rvodevelopment.tuinmaat.service

import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun getLocalSettings(): StorageService {
    return object : KoinComponent {
        val service: StorageService = get<AndroidStorageService>()
    }.service
}
