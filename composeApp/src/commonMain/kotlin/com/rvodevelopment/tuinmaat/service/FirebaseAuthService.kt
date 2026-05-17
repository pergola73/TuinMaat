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
            firestore.collection("users").document(user.uid).set(profile)
            
            Result.success(UserProfile(user.uid, email, voornaam, achternaam))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(): Result<UserProfile> {
        return try {
            val idToken = getGoogleIdToken() ?: throw Exception("Google login geannuleerd")
            val credential = dev.gitlive.firebase.auth.GoogleAuthProvider.credential(idToken, null)
            val result = auth.signInWithCredential(credential)
            val user = result.user ?: throw Exception("Firebase login mislukt")
            
            // Controleer of gebruiker al bestaat in Firestore, zo niet: aanmaken
            val doc = firestore.collection("users").document(user.uid).get()
            if (!doc.exists) {
                val names = user.displayName?.split(" ")
                val voornaam = names?.firstOrNull() ?: ""
                val achternaam = names?.drop(1)?.joinToString(" ") ?: ""
                
                val profile = mapOf(
                    "voornaam" to voornaam,
                    "achternaam" to achternaam,
                    "email" to (user.email ?: ""),
                    "tuinNaam" to "Mijn Tuin",
                    "biometrieIngeschakeld" to false,
                    "locaties" to listOf("Tuin", "Balkon", "Kas"),
                    "standaardLocatie" to "Tuin"
                )
                firestore.collection("users").document(user.uid).set(profile)
            }

            Result.success(UserProfile(user.uid, user.email, null, null))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            auth.currentUser?.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
