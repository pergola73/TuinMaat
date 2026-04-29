package com.rvodevelopment.tuinmaat.repository

import com.rvodevelopment.tuinmaat.model.Plant
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseTuinRepository : TuinRepository {
    private val firestore = Firebase.firestore

    override suspend fun createInitialTuin(userId: String): Result<Unit> {
        return try {
            firestore.collection("tuinen").document(userId).set(
                mapOf(
                    "eigenaar" to userId,
                    "naam" to "Mijn Tuin"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlanten(gardenId: String): Flow<List<Plant>> {
        return firestore.collection("tuinen").document(gardenId).collection("planten")
            .snapshots().map { snapshot ->
                snapshot.documents.map { doc ->
                    try {
                        doc.data<Plant>().copy(firestoreId = doc.id)
                    } catch (e: Exception) {
                        val data = doc.data<Map<String, Any?>>()
                        Plant(
                            firestoreId = doc.id,
                            naam = data["naam"] as? String ?: "",
                            omschrijving = data["omschrijving"] as? String ?: "",
                            snoeiMaand = data["snoeiMaand"] as? String ?: "",
                            locatie = data["locatie"] as? String ?: "Tuin",
                            fotoUri = data["fotoUri"] as? String
                        )
                    }
                }
            }
    }

    override suspend fun getPlant(gardenId: String, plantId: String): Flow<Plant?> {
        return firestore.collection("tuinen").document(gardenId).collection("planten").document(plantId)
            .snapshots().map { snapshot ->
                if (snapshot.exists) {
                    try {
                        snapshot.data<Plant>().copy(firestoreId = snapshot.id)
                    } catch (e: Exception) {
                        val data = snapshot.data<Map<String, Any?>>()
                        Plant(
                            firestoreId = snapshot.id,
                            naam = data["naam"] as? String ?: "",
                            omschrijving = data["omschrijving"] as? String ?: "",
                            snoeiMaand = data["snoeiMaand"] as? String ?: "",
                            locatie = data["locatie"] as? String ?: "Tuin",
                            fotoUri = data["fotoUri"] as? String
                        )
                    }
                } else {
                    null
                }
            }
    }

    override suspend fun deletePlant(gardenId: String, plantId: String): Result<Unit> {
        return try {
            firestore.collection("tuinen").document(gardenId).collection("planten").document(plantId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePlant(gardenId: String, plant: Plant): Result<Unit> {
        return try {
            val collection = firestore.collection("tuinen").document(gardenId).collection("planten")
            if (plant.firestoreId.isEmpty()) {
                collection.add(plant)
            } else {
                collection.document(plant.firestoreId).set(plant)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTuinnaam(gardenId: String): Flow<String> {
        return firestore.collection("tuinen").document(gardenId).snapshots().map { snapshot ->
            snapshot.get<String?>("naam") ?: "Mijn Tuin"
        }
    }

    override fun getLocaties(gardenId: String): Flow<List<String>> {
        return firestore.collection("tuinen").document(gardenId).collection("locaties")
            .snapshots().map { snapshot ->
                snapshot.documents.map { it.get<String>("naam") ?: "" }.filter { it.isNotEmpty() }
            }
    }

    override suspend fun voegLocatieToe(gardenId: String, naam: String): Result<Unit> {
        return try {
            firestore.collection("tuinen").document(gardenId).collection("locaties")
                .document(naam).set(mapOf("naam" to naam))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verwijderLocatie(gardenId: String, naam: String): Result<Unit> {
        return try {
            firestore.collection("tuinen").document(gardenId).collection("locaties")
                .document(naam).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun migrateLegacyData(userId: String, gardenId: String): Result<Unit> {
        return try {
            // 1. Haal de 'locaties' lijst op uit het user document (oude stijl)
            val userDoc = firestore.collection("users").document(userId).get()
            val legacyLocatiesLijst = (userDoc.get<List<*>>("locaties"))?.filterIsInstance<String>() ?: emptyList()

            // 2. Migreer deze lijst naar de nieuwe subcollectie in de tuin
            legacyLocatiesLijst.forEach { naam ->
                voegLocatieToe(gardenId, naam)
            }

            // 3. Haal locaties op uit de oude subcollectie users/{userId}/locaties
            val legacyLocatiesDocs = firestore.collection("users").document(userId).collection("locaties").get()
            legacyLocatiesDocs.documents.forEach { doc ->
                val naam = doc.get<String?>("naam") ?: doc.id
                voegLocatieToe(gardenId, naam)
            }

            // 4. Haal planten op uit de oude subcollectie users/{userId}/planten
            val legacyPlantenDocs = firestore.collection("users").document(userId).collection("planten").get()
            legacyPlantenDocs.documents.forEach { doc ->
                val plantData = doc.data<Map<String, Any?>>()
                firestore.collection("tuinen").document(gardenId).collection("planten").document(doc.id).set(plantData)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
