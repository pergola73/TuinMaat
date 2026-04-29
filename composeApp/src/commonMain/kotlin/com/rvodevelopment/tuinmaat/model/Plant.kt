package com.rvodevelopment.tuinmaat.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "planten_tabel")
@Serializable
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
    val waterBehoefte: String = "",
    val lichtBehoefte: String = "",
    val voedingAdvies: String = "",
    val ehboSignaal: String = "",
    val wetenschappelijkeNaam: String = "",
    val fotoUri: String? = null,
    val locatie: String = "",
    val bron: String = ""
)
