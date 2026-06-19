package com.rvodevelopment.tuinmaat.service

class IosAiService : AiService {
    override suspend fun identifyPlant(imageBytes: ByteArray): Result<AiPlantResult> {
        return Result.failure(Exception("iOS AI Service niet geïmplementeerd"))
    }

    override suspend fun identifyPlantByName(name: String): Result<AiPlantResult> {
        return Result.failure(Exception("iOS AI Service niet geïmplementeerd"))
    }

    override suspend fun identifyDisease(imageBytes: ByteArray, plantName: String?): Result<AiDiseaseResult> {
        return Result.failure(Exception("iOS Disease Identification niet geïmplementeerd"))
    }

    override suspend fun generateGardenTip(plantNames: List<String>): Result<AiGardenTip> {
        return Result.failure(Exception("iOS Garden Tip niet geïmplementeerd"))
    }
}
