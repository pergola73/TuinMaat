package com.rvodevelopment.tuinmaat.data

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.rvodevelopment.tuinmaat.model.Locatie
import com.rvodevelopment.tuinmaat.model.Plant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [Plant::class, Locatie::class], version = 2, exportSchema = false)
@ConstructedBy(PlantDatabaseConstructor::class)
abstract class PlantDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
}

/**
 * Gebruikt door platform-specifieke builders
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object PlantDatabaseConstructor : RoomDatabaseConstructor<PlantDatabase>

expect fun getInMemoryDatabaseBuilder(): RoomDatabase.Builder<PlantDatabase>

fun getRoomDatabase(
    builder: RoomDatabase.Builder<PlantDatabase>
): PlantDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
