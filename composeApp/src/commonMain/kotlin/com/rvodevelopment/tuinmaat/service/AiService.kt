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

data class AiGardenTip(
    val temperatuur: Int,
    val conditie: String,
    val icoon: String,
    val tip: String
)

interface AiService {
    suspend fun identifyPlant(imageBytes: ByteArray): Result<AiPlantResult>
    suspend fun generateGardenTip(plantNames: List<String> = emptyList()): Result<AiGardenTip>
}
