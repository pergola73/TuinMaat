package com.example.tuinmaat

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Plant::class, Locatie::class], version = 3, exportSchema = false)
abstract class PlantDatabase : RoomDatabase() {

    abstract fun plantDao(): PlantDao

    companion object {
        @Volatile
        private var INSTANCE: PlantDatabase? = null

        fun getDatabase(context: Context): PlantDatabase {
            // Als de INSTANCE niet null is, geef hem terug.
            // Zo niet, maak dan de database aan.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlantDatabase::class.java,
                    "plant_database"
                ).fallbackToDestructiveMigration() // Dit voorkomt crashes bij updates tijdens het bouwen
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}