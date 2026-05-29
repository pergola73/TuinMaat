@file:JvmName("PlantDatabaseAndroidKt")
package com.rvodevelopment.tuinmaat.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

import com.rvodevelopment.tuinmaat.util.ActivityProvider

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<PlantDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("planten_database.db")
    return Room.databaseBuilder<PlantDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
        factory = { PlantDatabaseConstructor.initialize() }
    )
}

actual fun getInMemoryDatabaseBuilder(): RoomDatabase.Builder<PlantDatabase> {
    val context = ActivityProvider.getCurrentActivity()?.applicationContext 
        ?: throw IllegalStateException("Android in-memory builder requires a context. ActivityProvider not initialized?")
    return Room.inMemoryDatabaseBuilder<PlantDatabase>(
        context = context,
        factory = { PlantDatabaseConstructor.initialize() }
    )
}
