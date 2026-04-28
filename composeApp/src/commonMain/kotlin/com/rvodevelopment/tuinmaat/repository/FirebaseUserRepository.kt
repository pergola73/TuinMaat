package com.rvodevelopment.tuinmaat.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseUserRepository : UserRepository {
    private val firestore = Firebase.firestore

    override fun getUserData(uid: String): Flow<UserData?> {
        return firestore.collection("gebruikers").document(uid).snapshots().map { snapshot ->
            if (snapshot.exists) {
                val data = snapshot.data<Map<String, Any?>>()
                UserData(
                    voornaam = data["voornaam"] as? String ?: "",
                    achternaam = data["achternaam"] as? String ?: "",
                    email = data["email"] as? String ?: "",
                    tuinnaam = data["tuinnaam"] as? String ?: "Mijn Tuin",
                    sharedGardenId = data["sharedGardenId"] as? String,
                    biometrieIngeschakeld = data["biometrieIngeschakeld"] as? Boolean ?: false,
                    locaties = (data["locaties"] as? List<*>)?.filterIsInstance<String>() ?: listOf("Tuin", "Balkon", "Kas"),
                    standaardLocatie = data["standaardLocatie"] as? String ?: "Tuin"
                )
            } else {
                null
            }
        }
    }

    override suspend fun updateSharedGardenId(uid: String, sharedGardenId: String?): Result<Unit> {
        return try {
            firestore.collection("gebruikers").document(uid)
                .update("sharedGardenId" to sharedGardenId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(uid: String, voornaam: String, achternaam: String, tuinnaam: String): Result<Unit> {
        return try {
            firestore.collection("gebruikers").document(uid).update(
                "voornaam" to voornaam,
                "achternaam" to achternaam,
                "tuinnaam" to tuinnaam
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBiometrie(uid: String, ingeschakeld: Boolean): Result<Unit> {
        return try {
            firestore.collection("gebruikers").document(uid)
                .update("biometrieIngeschakeld" to ingeschakeld)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLocaties(uid: String, locaties: List<String>, standaardLocatie: String): Result<Unit> {
        return try {
            firestore.collection("gebruikers").document(uid).update(
                "locaties" to locaties,
                "standaardLocatie" to standaardLocatie
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlinkGarden(uid: String): Result<Unit> {
        return try {
            firestore.collection("gebruikers").document(uid)
                .update("sharedGardenId" to null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
