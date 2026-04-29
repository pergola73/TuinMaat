package com.rvodevelopment.tuinmaat.repository

import com.rvodevelopment.tuinmaat.model.Plant
import kotlinx.coroutines.flow.Flow

interface TuinRepository {
    suspend fun createInitialTuin(userId: String): Result<Unit>
    suspend fun getPlanten(gardenId: String): Flow<List<Plant>>
    suspend fun getPlant(gardenId: String, plantId: String): Flow<Plant?>
    suspend fun deletePlant(gardenId: String, plantId: String): Result<Unit>
    suspend fun savePlant(gardenId: String, plant: Plant): Result<Unit>
    suspend fun getTuinnaam(gardenId: String): Flow<String>
    
    // Nieuwe methoden voor de locaties subcollectie
    fun getLocaties(gardenId: String): Flow<List<String>>
    suspend fun voegLocatieToe(gardenId: String, naam: String): Result<Unit>
    suspend fun verwijderLocatie(gardenId: String, naam: String): Result<Unit>

    // Migratie
    suspend fun migrateLegacyData(userId: String, gardenId: String): Result<Unit>
}
