package com.rvodevelopment.tuinmaat.data

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.rvodevelopment.tuinmaat.model.Plant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PlantDatabaseTest {
    private lateinit var db: PlantDatabase
    private lateinit var dao: PlantDao

    @BeforeTest
    fun createDb() {
        val builder: RoomDatabase.Builder<PlantDatabase> = Room.inMemoryDatabaseBuilder<PlantDatabase>()
        db = builder
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        dao = db.plantDao()
    }

    @AfterTest
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadPlant() = runTest {
        val plant = Plant(
            id = 1,
            naam = "Test Plant",
            wetenschappelijkeNaam = "Testus Plantus",
            omschrijving = "Een test plant",
            lichtBehoefte = "Volle zon",
            waterBehoefte = "Wekelijks",
            snoeiMaand = "Maart",
            bemesting = "Geen"
        )
        dao.insertPlant(plant)
        val allPlanten = dao.getAllPlanten().first()
        assertEquals(1, allPlanten.size)
        assertEquals("Test Plant", allPlanten[0].naam)
    }
}
