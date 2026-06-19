package com.rvodevelopment.tuinmaat.model

import kotlinx.serialization.Serializable

@Serializable
data class Diagnosis(
    val id: String = "",
    val timestamp: Long = 0,
    val ziekteNaam: String = "",
    val omschrijving: String = "",
    val advies: String = "",
    val fotoUrl: String? = null, // De foto die de gebruiker nam
    val referentieFoto: String? = null,
    val plantId: String? = null // Optioneel: gekoppeld aan een specifieke plant
)
