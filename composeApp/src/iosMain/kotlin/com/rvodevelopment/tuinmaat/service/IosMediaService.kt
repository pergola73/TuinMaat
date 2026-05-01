package com.rvodevelopment.tuinmaat.service

class IosMediaService : MediaService {
    override suspend fun pickImage(): ByteArray? = null
    override suspend fun takePhoto(): ByteArray? = null
    override suspend fun resizeImage(imageBytes: ByteArray, maxDimension: Int): ByteArray = imageBytes
}
