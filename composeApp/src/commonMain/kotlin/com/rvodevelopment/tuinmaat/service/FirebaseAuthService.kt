package com.rvodevelopment.tuinmaat.service

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseAuthService : AuthService {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    override val currentUser: Flow<UserProfile?> = auth.authStateChanged.map { user ->
        user?.let {
            UserProfile(it.uid, it.email, null, null)
        }
    }

    override suspend fun signIn(email: String, wachtwoord: String): Result<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, wachtwoord)
            val user = result.user ?: throw Exception("Inloggen mislukt")
            Result.success(UserProfile(user.uid, user.email, null, null))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, wachtwoord: String, voornaam: String, achternaam: String): Result<UserProfile> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, wachtwoord)
            val user = result.user ?: throw Exception("Registratie mislukt")
            
            val profile = mapOf(
                "voornaam" to voornaam,
                "achternaam" to achternaam,
                "email" to email
            )
            firestore.collection("gebruikers").document(user.uid).set(profile)
            
            Result.success(UserProfile(user.uid, email, voornaam, achternaam))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(): Result<UserProfile> {
        return Result.failure(Exception("Google Sign-In vereist platform-specifieke implementatie"))
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
