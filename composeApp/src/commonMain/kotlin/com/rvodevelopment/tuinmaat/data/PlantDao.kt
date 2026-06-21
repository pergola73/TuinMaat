package com.rvodevelopment.tuinmaat.data

import androidx.room.*
import com.rvodevelopment.tuinmaat.model.Locatie
import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.model.VoltooideTaak
import com.rvodevelopment.tuinmaat.model.HandmatigeTaak
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    @Query("SELECT * FROM planten_tabel ORDER BY naam ASC")
    fun getAllPlanten(): Flow<List<Plant>>

    @Query("SELECT * FROM planten_tabel WHERE snoeiMaand LIKE '%' || :maand || '%' ORDER BY naam ASC")
    fun getPlantenVoorMaand(maand: String): Flow<List<Plant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant)

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markeerTaakVoltooid(taak: VoltooideTaak)

    @Query("SELECT taakId FROM voltooide_taken")
    fun getVoltooideTaakIds(): kotlinx.coroutines.flow.Flow<List<String>>

    @Query("DELETE FROM voltooide_taken WHERE taakId = :id")
    suspend fun deactiveerTaak(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHandmatigeTaak(taak: HandmatigeTaak)

    @Query("SELECT * FROM handmatige_taken WHERE datum = :datum")
    fun getHandmatigeTakenVoorDatum(datum: String): Flow<List<HandmatigeTaak>>

    @Query("UPDATE handmatige_taken SET isVoltooid = :voltooid WHERE id = :id")
    suspend fun updateHandmatigeTaakStatus(id: Int, voltooid: Boolean)

    @Delete
    suspend fun deleteHandmatigeTaak(taak: HandmatigeTaak)
}
