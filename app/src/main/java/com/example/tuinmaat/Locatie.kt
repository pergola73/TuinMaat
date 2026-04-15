package com.example.tuinmaat // Let op: gebruik je eigen pakketnaam bovenaan!

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locatie_tabel")
data class Locatie(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val naam: String
)