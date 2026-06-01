package com.rvodevelopment.tuinmaat.service

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage
import dev.gitlive.firebase.storage.StorageReference

// Unieke naam om conflicten met platform-specifieke SDK's te voorkomen
expect suspend fun StorageReference.performByteArrayUpload(bytes: ByteArray)

expect fun getLocalSettings(): StorageService

class FirebaseStorageService : StorageService {
    private val storage = Firebase.storage
    private val localSettings by lazy { getLocalSettings() }

    override fun getString(key: String, defaultValue: String): String = 
        localSettings.getString(key, defaultValue)

    override fun setString(key: String, value: String) = 
        localSettings.setString(key, value)

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = 
        localSettings.getBoolean(key, defaultValue)

    override fun setBoolean(key: String, value: Boolean) = 
        localSettings.setBoolean(key, value)

    override fun remove(key: String) = 
        localSettings.remove(key)

    override suspend fun uploadFile(path: String, bytes: ByteArray): Result<String> {
        return try {
            val ref = storage.reference(path)
            ref.performByteArrayUpload(bytes)
            Result.success(ref.getDownloadUrl())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
