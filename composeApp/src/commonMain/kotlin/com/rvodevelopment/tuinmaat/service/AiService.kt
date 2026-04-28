package com.rvodevelopment.tuinmaat.service

import com.rvodevelopment.tuinmaat.model.Plant

data class AiPlantResult(
    val naam: String = "",
    val wetenschappelijkeNaam: String = "",
    val omschrijving: String = "",
    val snoeiAdvies: String = "",
    val snoeiMaand: String = "",
    val waterBehoefte: String = "",
    val lichtBehoefte: String = "",
    val voedingAdvies: String = "",
    val bemesting: String = "",
    val ehboSignaal: String = "",
    val bron: String = ""
)

interface AiService {
    suspend fun identifyPlant(imageBytes: ByteArray): Result<AiPlantResult>
}
