package com.rvodevelopment.tuinmaat.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseUserRepository : UserRepository {
    private val firestore = Firebase.firestore

    override fun getUserData(uid: String): Flow<UserData?> {
        return firestore.collection("users").document(uid).snapshots().map { snapshot ->
            if (snapshot.exists) {
                try {
                    snapshot.data<UserData>().copy(id = snapshot.id)
                } catch (e: Exception) {
                    val data = snapshot.data<Map<String, Any?>>()
                    UserData(
                        id = snapshot.id,
                        voornaam = data["voornaam"] as? String ?: (data["name"] as? String)?.split(" ")?.firstOrNull() ?: "",
                        achternaam = data["achternaam"] as? String ?: (data["name"] as? String)?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                        email = data["email"] as? String ?: "",
                        tuinnaam = data["tuinNaam"] as? String ?: "Mijn Tuin",
                        sharedGardenId = data["sharedGardenId"] as? String,
                        biometrieIngeschakeld = data["biometrieIngeschakeld"] as? Boolean ?: false,
                        securityType = data["securityType"] as? String ?: "NONE",
                        securityPin = data["securityPin"] as? String ?: "",
                        activeGardenId = data["activeGardenId"] as? String,
                        locaties = data["locaties"] as? List<String> ?: listOf("Tuin", "Balkon", "Kas"),
                        standaardLocatie = data["standaardLocatie"] as? String ?: "Tuin"
                    )
                }
            } else {
                null
            }
        }
    }

    override suspend fun updateSharedGardenId(uid: String, sharedGardenId: String?): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .set(mapOf(
                    "sharedGardenId" to sharedGardenId,
                    "activeGardenId" to sharedGardenId
                ), merge = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(uid: String, voornaam: String, achternaam: String, tuinnaam: String): Result<Unit> {
        return try {
            val data = mapOf(
                "voornaam" to voornaam,
                "achternaam" to achternaam,
                "name" to "$voornaam $achternaam",
                "tuinNaam" to tuinnaam
            )
            firestore.collection("users").document(uid).set(data, merge = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBiometrie(uid: String, ingeschakeld: Boolean): Result<Unit> {
        return try {
            val data = mapOf("biometrieIngeschakeld" to ingeschakeld)
            firestore.collection("users").document(uid).set(data, merge = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLocaties(uid: String, locaties: List<String>, standaardLocatie: String): Result<Unit> {
        return try {
            val data = mapOf(
                "locaties" to locaties,
                "standaardLocatie" to standaardLocatie
            )
            firestore.collection("users").document(uid).set(data, merge = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlinkGarden(uid: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .set(mapOf("sharedGardenId" to null), merge = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setActiveGarden(uid: String, gardenId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .set(mapOf("activeGardenId" to gardenId), merge = true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
