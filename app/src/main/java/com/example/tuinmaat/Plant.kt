package com.example.tuinmaat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planten_tabel")
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firestoreId: String = "",
    val naam: String = "",
    val omschrijving: String = "",
    val snoeiMaand: String = "",
    val bemesting: String = "",
    val snoeiInstructies: String = "",
    val snoeiAdvies: String = "",
    val fotoUri: String? = null,
    val locatie: String = ""
)
