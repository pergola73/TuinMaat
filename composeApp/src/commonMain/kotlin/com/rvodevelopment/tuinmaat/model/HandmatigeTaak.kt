package com.rvodevelopment.tuinmaat.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "handmatige_taken")
@Serializable
data class HandmatigeTaak(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titel: String,
    val omschrijving: String = "",
    val datum: String, // YYYY-MM-DD
    val isVoltooid: Boolean = false,
    val plantId: String? = null // Optionele koppeling aan een plant
)
