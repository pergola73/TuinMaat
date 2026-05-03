package com.rvodevelopment.tuinmaat.data

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import platform.Foundation.NSHomeDirectory

@Suppress("NO_ACTUAL_FOR_EXPECT")
actual object PlantDatabaseConstructor : RoomDatabaseConstructor<PlantDatabase> {
    override fun initialize(): PlantDatabase {
        throw NotImplementedError("Database is niet beschikbaar op CI Simulator build")
    }
}

fun getDatabaseBuilder(): RoomDatabase.Builder<PlantDatabase> {
    val dbFile = NSHomeDirectory() + "/planten_database.db"
    return Room.databaseBuilder<PlantDatabase>(
        name = dbFile,
        factory = { PlantDatabaseConstructor.initialize() }
    )
}
