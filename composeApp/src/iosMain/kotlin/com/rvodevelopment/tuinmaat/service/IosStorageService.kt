package com.rvodevelopment.tuinmaat.service

import platform.Foundation.NSUserDefaults

class IosStorageService : StorageService {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String, defaultValue: String): String {
        return defaults.stringForKey(key) ?: defaultValue
    }

    override fun setString(key: String, value: String) {
        defaults.setObject(value, key)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (defaults.objectForKey(key) == null) defaultValue else defaults.boolForKey(key)
    }

    override fun setBoolean(key: String, value: Boolean) {
        defaults.setBool(value, key)
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
    
    override suspend fun uploadFile(path: String, bytes: ByteArray): Result<String> {
        return Result.failure(Exception("Not implemented for iOS yet"))
    }
}
