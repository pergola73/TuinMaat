package com.rvodevelopment.tuinmaat.service

class IosAiService : AiService {
    override suspend fun identifyPlant(imageBytes: ByteArray): Result<AiPlantResult> {
        // Placeholder for Firebase Vertex AI iOS implementation
        return Result.failure(Exception("iOS AI Service niet geïmplementeerd"))
    }
}
