package com.rvodevelopment.tuinmaat.premium.health

import com.rvodevelopment.tuinmaat.service.AiDiseaseResult
import com.rvodevelopment.tuinmaat.service.AiService

/**
 * Service voor ziekteherkenning en verzorgingsadvies (Dr. Tuinmaat).
 */
class DrTuinmaatService(
    private val aiService: AiService
) {
    suspend fun diagnosePlant(imageBytes: ByteArray, currentPlantName: String? = null): Result<AiDiseaseResult> {
        return aiService.identifyDisease(imageBytes, currentPlantName)
    }
}
