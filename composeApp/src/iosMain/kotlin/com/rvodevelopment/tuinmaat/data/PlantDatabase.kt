package com.rvodevelopment.tuinmaat.data

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<PlantDatabase> {
    val dbFile = NSHomeDirectory() + "/planten_database.db"
    return Room.databaseBuilder<PlantDatabase>(
        name = dbFile,
        factory = { PlantDatabaseConstructor.initialize() }
    )
}
