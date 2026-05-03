package com.rvodevelopment.tuinmaat.service

class IosAiService : AiService {
    override suspend fun identifyPlant(imageBytes: ByteArray): Result<AiPlantResult> {
        return Result.failure(Exception("iOS AI Service niet geïmplementeerd"))
    }

    override suspend fun generateGardenTip(plantNames: List<String>): Result<AiGardenTip> {
        return Result.failure(Exception("iOS Garden Tip niet geïmplementeerd"))
    }
}
