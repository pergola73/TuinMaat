package com.rvodevelopment.tuinmaat.service

import kotlinx.coroutines.flow.MutableStateFlow

class MockAuthService : AuthService {
    override val currentUser = MutableStateFlow<UserProfile?>(null)
    override suspend fun signIn(email: String, wachtwoord: String) = Result.success(UserProfile("1", "test@test.nl", "Test", "User"))
    override suspend fun signUp(email: String, wachtwoord: String, voornaam: String, achternaam: String) = Result.success(UserProfile("1", email, voornaam, achternaam))
    override suspend fun signInWithGoogle() = Result.success(UserProfile("1", "test@test.nl", "Test", "User"))
    override suspend fun signOut() {}
    override suspend fun deleteAccount() = Result.success(Unit)
    override suspend fun sendPasswordResetEmail(email: String) = Result.success(Unit)
    override suspend fun sendEmailVerification() = Result.success(Unit)
}

class MockStorageService : StorageService {
    override fun getString(key: String, defaultValue: String) = ""
    override fun setString(key: String, value: String) {}
    override fun getBoolean(key: String, defaultValue: Boolean) = false
    override fun setBoolean(key: String, value: Boolean) {}
    override fun remove(key: String) {}
    override suspend fun uploadFile(path: String, bytes: ByteArray) = Result.success("https://dummy.url")
}
