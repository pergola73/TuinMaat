package com.rvodevelopment.tuinmaat.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommonAiServiceTest {

    @Test
    fun testIdentifyPlantWithMockResponse() = runTest {
        val mockMediaService = object : MediaService {
            override suspend fun pickImage(): ByteArray? = null
            override suspend fun takePhoto(): ByteArray? = null
            override suspend fun resizeImage(imageBytes: ByteArray, maxDimension: Int): ByteArray = imageBytes
        }

        val mockEngine = MockEngine { request ->
            when {
                request.url.toString().contains("plantnet") -> {
                    respond(
                        content = """{"results":[{"species":{"commonNames":["Test Plant"],"scientificNameWithoutAuthor":"Testus Plantus"}}]}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                request.url.toString().contains("wikipedia") -> {
                    respond(
                        content = """{"extract":"Wikipedia info over test plant"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                request.url.toString().contains("generativelanguage") -> {
                    respond(
                        content = """{
                            "candidates": [{
                                "content": {
                                    "parts": [{
                                        "text": "{\"omschrijving\": \"Test omschrijving\", \"waterBehoefte\": \"Veel\", \"lichtBehoefte\": \"Zon\"}"
                                    }]
                                }
                            }]
                        }""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> respond(
                    content = "",
                    status = HttpStatusCode.NotFound
                )
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val service = CommonAiService(
            httpClient,
            "mock-plantnet-key",
            "mock-gemini-key",
            "gemini-flash-latest",
            mockMediaService
        )

        val result = service.identifyPlant(byteArrayOf(0, 1, 2))
        
        assertTrue(result.isSuccess, "Result should be success")
        val plantResult = result.getOrThrow()
        assertEquals("Test Plant", plantResult.naam)
        assertEquals("Test omschrijving", plantResult.omschrijving)
    }
}
