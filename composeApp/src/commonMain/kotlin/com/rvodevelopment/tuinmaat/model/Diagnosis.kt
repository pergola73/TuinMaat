package com.rvodevelopment.tuinmaat.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "diagnoses")
@Serializable
data class Diagnosis(
    @PrimaryKey
    val id: String = "",
    val timestamp: Long = 0,
    val ziekteNaam: String = "",
    val omschrijving: String = "",
    val advies: String = "",
    val fotoUrl: String? = null,
    val referentieFoto: String? = null,
    val plantId: String? = null
)
