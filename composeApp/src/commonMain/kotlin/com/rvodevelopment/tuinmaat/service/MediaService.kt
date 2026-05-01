package com.rvodevelopment.tuinmaat.service

interface MediaService {
    suspend fun pickImage(): ByteArray?
    suspend fun takePhoto(): ByteArray?
    suspend fun resizeImage(imageBytes: ByteArray, maxDimension: Int): ByteArray
}
