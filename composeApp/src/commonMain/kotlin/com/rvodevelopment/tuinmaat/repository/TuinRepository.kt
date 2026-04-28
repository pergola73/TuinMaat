package com.rvodevelopment.tuinmaat.repository

import com.rvodevelopment.tuinmaat.model.Plant
import kotlinx.coroutines.flow.Flow

interface TuinRepository {
    suspend fun createInitialTuin(userId: String): Result<Unit>
    suspend fun getPlanten(userId: String): Flow<List<Plant>>
    suspend fun getPlant(userId: String, plantId: String): Flow<Plant?>
    suspend fun deletePlant(userId: String, plantId: String): Result<Unit>
    suspend fun savePlant(userId: String, plant: Plant): Result<Unit>
}
