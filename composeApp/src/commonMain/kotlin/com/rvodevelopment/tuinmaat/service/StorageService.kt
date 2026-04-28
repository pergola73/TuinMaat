package com.rvodevelopment.tuinmaat.service

interface StorageService {
    fun getString(key: String, defaultValue: String = ""): String
    fun setString(key: String, value: String)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun setBoolean(key: String, value: Boolean)
    fun remove(key: String)
    suspend fun uploadFile(path: String, bytes: ByteArray): Result<String>
}
