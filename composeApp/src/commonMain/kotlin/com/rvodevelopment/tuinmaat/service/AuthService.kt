package com.rvodevelopment.tuinmaat.service

import kotlinx.coroutines.flow.Flow

data class UserProfile(
    val uid: String,
    val email: String?,
    val voornaam: String?,
    val achternaam: String?
)

interface AuthService {
    val currentUser: Flow<UserProfile?>
    
    suspend fun signIn(email: String, wachtwoord: String): Result<UserProfile>
    suspend fun signUp(email: String, wachtwoord: String, voornaam: String, achternaam: String): Result<UserProfile>
    suspend fun signInWithGoogle(): Result<UserProfile>
    suspend fun signOut()
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendEmailVerification(): Result<Unit>
}
