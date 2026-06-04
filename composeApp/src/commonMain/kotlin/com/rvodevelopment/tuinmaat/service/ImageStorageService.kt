package com.rvodevelopment.tuinmaat.service

interface ImageStorageService {
    suspend fun uploadFile(path: String, bytes: ByteArray): Result<String>
}
