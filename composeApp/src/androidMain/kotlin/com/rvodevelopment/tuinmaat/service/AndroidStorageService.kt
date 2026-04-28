package com.rvodevelopment.tuinmaat.service

import android.content.Context
import android.content.SharedPreferences

class AndroidStorageService(context: Context) : StorageService {
    private val prefs: SharedPreferences = context.getSharedPreferences("tuinmaat_prefs", Context.MODE_PRIVATE)

    override fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    override fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    override fun setBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    override suspend fun uploadFile(path: String, bytes: ByteArray): Result<String> {
        // Deze service lijkt een SharedPreferences wrapper te zijn ondanks de naam.
        // Voor werkelijke file upload gebruiken we FirebaseStorageService.
        return Result.failure(Exception("Niet geïmplementeerd in AndroidStorageService"))
    }
}
