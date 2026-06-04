package com.rvodevelopment.tuinmaat.service

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage
import dev.gitlive.firebase.storage.StorageReference

// Unieke naam om conflicten met platform-specifieke SDK's te voorkomen
expect suspend fun StorageReference.performByteArrayUpload(bytes: ByteArray)

class FirebaseStorageService : ImageStorageService {
    private val storage = Firebase.storage

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
