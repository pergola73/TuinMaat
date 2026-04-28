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

    override suspend fun getPlanten(userId: String): Flow<List<Plant>> {
        val userDoc = firestore.collection("gebruikers").document(userId).get()
        val gardenId = userDoc.get<String?>("sharedGardenId") ?: userId
        
        return firestore.collection("tuinen").document(gardenId).collection("planten")
            .snapshots().map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Plant>().copy(firestoreId = doc.id)
                }
            }
    }

    override suspend fun getPlant(userId: String, plantId: String): Flow<Plant?> {
        val userDoc = firestore.collection("gebruikers").document(userId).get()
        val gardenId = userDoc.get<String?>("sharedGardenId") ?: userId

        return firestore.collection("tuinen").document(gardenId).collection("planten").document(plantId)
            .snapshots().map { snapshot ->
                if (snapshot.exists) {
                    snapshot.data<Plant>().copy(firestoreId = snapshot.id)
                } else {
                    null
                }
            }
    }

    override suspend fun deletePlant(userId: String, plantId: String): Result<Unit> {
        return try {
            val userDoc = firestore.collection("gebruikers").document(userId).get()
            val gardenId = userDoc.get<String?>("sharedGardenId") ?: userId
            firestore.collection("tuinen").document(gardenId).collection("planten").document(plantId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePlant(userId: String, plant: Plant): Result<Unit> {
        return try {
            val userDoc = firestore.collection("gebruikers").document(userId).get()
            val gardenId = userDoc.get<String?>("sharedGardenId") ?: userId
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
}
