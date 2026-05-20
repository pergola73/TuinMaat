package com.rvodevelopment.tuinmaat.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.FieldValue

class FirebaseUserRepository : UserRepository {
    private val firestore = Firebase.firestore

    override fun getUserData(uid: String): Flow<UserData?> {
        return firestore.collection("users").document(uid).snapshots()
            .map { snapshot ->
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
                            standaardLocatie = data["standaardLocatie"] as? String ?: "Tuin",
                            sharedByUsers = (data["sharedByUsers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        )
                    }
                } else {
                    null
                }
            }
            .catch { 
                println("FirebaseUserRepository: Error fetching snapshots: ${it.message}")
                emit(null) 
            }
    }

    override suspend fun updateSharedGardenId(uid: String, sharedGardenId: String?): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(uid)
            
            // 1. Als er al een oude koppeling was, die eerst opruimen bij de vorige eigenaar
            try {
                val currentData = userRef.get()
                val oldGardenId = currentData.get<String?>("sharedGardenId")
                if (oldGardenId != null && oldGardenId != uid) {
                    firestore.collection("users").document(oldGardenId)
                        .set(mapOf("sharedByUsers" to FieldValue.arrayRemove(uid)), merge = true)
                }
            } catch (e: Exception) {
                // Stille fout bij opruimen (bijv. geen rechten bij de vorige eigenaar)
                println("FirebaseUserRepository: Kon oude eigenaar niet informeren: ${e.message}")
            }

            // 2. Nieuwe koppeling leggen (DIT MOET LUKKEN)
            userRef.set(mapOf(
                "sharedGardenId" to sharedGardenId,
                "activeGardenId" to (sharedGardenId ?: uid)
            ), merge = true)

            // 3. Toevoegen aan de lijst van de nieuwe eigenaar
            if (sharedGardenId != null && sharedGardenId != uid) {
                try {
                    firestore.collection("users").document(sharedGardenId)
                        .set(mapOf("sharedByUsers" to FieldValue.arrayUnion(uid)), merge = true)
                } catch (e: Exception) {
                    // Stille fout: We hebben geen rechten bij de eigenaar, maar de koppeling bij onszelf staat!
                    println("FirebaseUserRepository: Geen rechten om nieuwe eigenaar te informeren, maar koppeling is geslaagd.")
                }
            }

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
        return updateSharedGardenId(uid, null)
    }

    override suspend fun removeViewerFromGarden(ownerUid: String, viewerUid: String): Result<Unit> {
        return try {
            // 1. Verwijder de koppeling bij de viewer
            try {
                firestore.collection("users").document(viewerUid)
                    .set(mapOf(
                        "sharedGardenId" to null,
                        "activeGardenId" to viewerUid
                    ), merge = true)
            } catch (e: Exception) {
                println("FirebaseUserRepository: Kon viewer niet ontkoppelen (geen rechten?), we gaan door met eigenaar-lijst.")
            }
            
            // 2. Verwijder de viewer uit de lijst van de eigenaar
            firestore.collection("users").document(ownerUid)
                .set(mapOf("sharedByUsers" to FieldValue.arrayRemove(viewerUid)), merge = true)

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

    override suspend fun deleteUserData(uid: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun triggerDeletionEmail(email: String, voornaam: String, reden: String): Result<Unit> {
        return try {
            // Mail naar de gebruiker
            firestore.collection("mail").add(mapOf(
                "to" to email,
                "message" to mapOf(
                    "subject" to "Bevestiging verwijdering TuinMaat account",
                    "text" to "Beste $voornaam,\n\nVia dit bericht bevestigen we dat je account en alle bijbehorende gegevens zijn verwijderd uit TuinMaat. We vinden het jammer dat je gaat, maar hopen dat je veel plezier hebt gehad van de app.\n\nMet groene groet,\nTeam TuinMaat"
                )
            ))
            
            // Mail naar de administrator
            firestore.collection("mail").add(mapOf(
                "to" to "rvanoel@etik.com",
                "message" to mapOf(
                    "subject" to "Account verwijderd: $voornaam",
                    "text" to "Gebruiker $voornaam ($email) heeft zijn account verwijderd.\nOpgegeven reden: $reden"
                )
            ))
            
            Result.success(Unit)
        } catch (e: Exception) {
            // We laten de app niet crashen als de mail-trigger faalt, maar loggen het wel
            println("Mail trigger error: ${e.message}")
            Result.success(Unit)
        }
    }
}
