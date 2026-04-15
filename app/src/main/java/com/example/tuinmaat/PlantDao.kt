package com.example.tuinmaat

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    // Haal alle planten op en sorteer ze op naam
    @Query("SELECT * FROM planten_tabel ORDER BY naam ASC")
    fun getAllPlanten(): Flow<List<Plant>>

    @Query("SELECT * FROM planten_tabel WHERE snoeiMaand = :maand ORDER BY naam ASC")
    fun getPlantenVoorMaand(maand: String): Flow<List<Plant>>

    // Voeg een nieuwe plant toe. Als hij al bestaat, overschrijf hem dan.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant)

    // Verwijder een plant uit de lijst
    @Delete
    suspend fun deletePlant(plant: Plant)

    @Query("SELECT * FROM locatie_tabel ORDER BY naam ASC")
    fun getAllLocaties(): Flow<List<Locatie>>

    @Insert
    suspend fun insertLocatie(locatie: Locatie)

    @Delete
    suspend fun deleteLocatie(locatie: Locatie)

    @Query("SELECT * FROM planten_tabel WHERE id = :id")
    suspend fun getPlantById(id: Int): Plant?
}
