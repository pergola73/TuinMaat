package com.rvodevelopment.tuinmaat.service

interface StorageService {
    fun getString(key: String, defaultValue: String = ""): String
    fun setString(key: String, value: String)
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun setBoolean(key: String, value: Boolean)
    fun getInt(key: String, defaultValue: Int = 0): Int
    fun setInt(key: String, value: Int)
    fun remove(key: String)
}
