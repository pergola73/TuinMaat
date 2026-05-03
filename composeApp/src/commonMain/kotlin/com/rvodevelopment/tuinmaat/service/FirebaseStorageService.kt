package com.rvodevelopment.tuinmaat.service

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage
import dev.gitlive.firebase.storage.StorageReference

// Platform-specifieke helper voor het uploaden van bytes naar Firebase Storage
expect suspend fun StorageReference.uploadBytes(bytes: ByteArray)

class FirebaseStorageService : StorageService {
    private val storage = Firebase.storage

    override fun getString(key: String, defaultValue: String): String = ""
    override fun setString(key: String, value: String) {}
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = false
    override fun setBoolean(key: String, value: Boolean) {}
    override fun remove(key: String) {}

    override suspend fun uploadFile(path: String, bytes: ByteArray): Result<String> {
        return try {
            val ref = storage.reference(path)
            ref.uploadBytes(bytes)
            Result.success(ref.getDownloadUrl())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
