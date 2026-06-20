package com.rvodevelopment.tuinmaat.data

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.rvodevelopment.tuinmaat.model.Diagnosis
import com.rvodevelopment.tuinmaat.model.Locatie
import com.rvodevelopment.tuinmaat.model.Plant
import com.rvodevelopment.tuinmaat.model.VoltooideTaak
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

import androidx.room.RoomDatabaseConstructor

@Database(entities = [Plant::class, Locatie::class, VoltooideTaak::class, Diagnosis::class], version = 5, exportSchema = true)
@ConstructedBy(PlantDatabaseConstructor::class)
abstract class PlantDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
}

expect fun getInMemoryDatabaseBuilder(): RoomDatabase.Builder<PlantDatabase>

fun getRoomDatabase(
    builder: RoomDatabase.Builder<PlantDatabase>
): PlantDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
}
