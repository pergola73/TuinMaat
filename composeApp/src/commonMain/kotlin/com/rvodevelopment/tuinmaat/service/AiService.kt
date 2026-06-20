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

data class AiDiseaseResult(
    val ziekteNaam: String,
    val score: Double,
    val eppoCode: String,
    val omschrijving: String = "",
    val advies: String = "",
    val referentieFoto: String? = null,
    val waterAnalyse: String = "",
    val lichtAnalyse: String = "",
    val voedingAnalyse: String = ""
)

interface AiService {
    suspend fun identifyPlant(imageBytes: ByteArray): Result<AiPlantResult>
    suspend fun identifyPlantByName(name: String): Result<AiPlantResult>
    suspend fun identifyDisease(imageBytes: ByteArray, plantName: String? = null): Result<AiDiseaseResult>
    suspend fun generateGardenTip(plantNames: List<String> = emptyList()): Result<AiGardenTip>
}
