@file:JvmName("PlantDatabaseAndroidKt")
package com.rvodevelopment.tuinmaat.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<PlantDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("planten_database.db")
    return Room.databaseBuilder<PlantDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
