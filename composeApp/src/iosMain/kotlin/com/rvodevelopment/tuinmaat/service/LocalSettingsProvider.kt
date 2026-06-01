package com.rvodevelopment.tuinmaat.service

actual fun getLocalSettings(): StorageService {
    return IosStorageService()
}
