package com.rvodevelopment.tuinmaat.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class CommonAiService(
    private val client: HttpClient,
    private val plantnetApiKey: String,
    private val geminiApiKey: String
) : AiService {

    override suspend fun identifyPlant(imageBytes: ByteArray): Result<AiPlantResult> = withContext(Dispatchers.IO) {
        try {
            val plantNetResult = identificeerMetPlantNet(imageBytes)
            val plantNaam = plantNetResult?.first ?: return@withContext Result.failure(Exception("Pl@ntNet kon de plant niet identificeren"))
            val scientificName = plantNetResult.second
            val wikipediaInfo = haalWikipediaInfoOp(plantNaam)
            val finaleInfo = verrijkMetGemini(plantNaam, wikipediaInfo)
            val bron = if (wikipediaInfo != null) "Pl@ntNet • Wikipedia/Gemini AI" else "Pl@ntNet • Gemini AI"
            
            Result.success(finaleInfo.copy(
                naam = plantNaam,
                wetenschappelijkeNaam = scientificName,
                bron = bron
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun identificeerMetPlantNet(imageBytes: ByteArray): Pair<String, String>? {
        return try {
            val response: String = client.submitFormWithBinaryData(
                url = "https://my-api.plantnet.org/v2/identify/all?api-key=$plantnetApiKey&lang=nl",
                formData = formData {
                    append("organs", "flower")
                    append("images", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"image.jpg\"")
                    })
                }
            ).body()

            val json = Json.parseToJsonElement(response).jsonObject
            val results = json["results"]?.jsonArray
            if (results != null && results.isNotEmpty()) {
                val bestMatch = results[0].jsonObject
                val species = bestMatch["species"]?.jsonObject
                val scientificName = species?.get("scientificNameWithoutAuthor")?.jsonPrimitive?.content ?: ""
                val commonNames = species?.get("commonNames")?.jsonArray
                val commonName = if (commonNames != null && commonNames.isNotEmpty()) commonNames[0].jsonPrimitive.content else scientificName
                Pair(commonName, scientificName)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun haalWikipediaInfoOp(naam: String): String? {
        return try {
            val encodedNaam = naam.replace(" ", "_")
            val response: JsonObject = client.get("https://nl.wikipedia.org/api/rest_v1/page/summary/$encodedNaam").body()
            response["extract"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun verrijkMetGemini(plantNaam: String, wikipediaInfo: String?): AiPlantResult {
        val wikiText = wikipediaInfo ?: "Geen Wikipedia informatie beschikbaar."
        val prompt = """
            Je bent een expert hovenier. 
            Gebruik deze informatie van Wikipedia: $wikiText
            
            Vul dit aan met jouw kennis voor de plant: $plantNaam.
            
            Geef de volgende details in het Nederlands:
            1. waterBehoefte: Hoe en wanneer water geven? (kort)
            2. lichtBehoefte: Wat is de beste plek? (zon/halfschaduw/schaduw)
            3. voedingAdvies: Wanneer heeft de plant voeding nodig?
            4. ehboSignaal: Waaraan zie je dat de plant ongezond is? (geel blad, hangend, etc.)
            5. snoeiMaand: Welke maanden snoeien? Geef een lijst van maanden gescheiden door komma's (bijv: Maart, April, Augustus).
            6. snoeiAdvies: Korte, praktische instructie (max 20 woorden).
            7. omschrijving: Een korte, pakkende tekst over de plant.
            8. bemesting: Welke specifieke voeding is het best?
            
            ANTWOORD UITSLUITEND IN DIT JSON FORMAAT:
            {
              "waterBehoefte": "...",
              "lichtBehoefte": "...",
              "voedingAdvies": "...",
              "ehboSignaal": "...",
              "snoeiMaand": "...",
              "snoeiAdvies": "...",
              "omschrijving": "...",
              "bemesting": "..."
            }
        """.trimIndent()

        return try {
            val response: JsonObject = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$geminiApiKey") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    putJsonArray("contents") {
                        addJsonObject {
                            putJsonArray("parts") {
                                addJsonObject {
                                    put("text", prompt)
                                }
                            }
                        }
                    }
                })
            }.body()

            val textResult = response["candidates"]?.jsonArray?.get(0)?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray?.get(0)?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: "{}"

            // Verwijder eventuele markdown code blocks die Gemini soms toevoegt
            val cleanJson = textResult
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = Json { ignoreUnknownKeys = true }.parseToJsonElement(cleanJson).jsonObject

            AiPlantResult(
                naam = plantNaam,
                omschrijving = json["omschrijving"]?.jsonPrimitive?.content ?: "",
                waterBehoefte = json["waterBehoefte"]?.jsonPrimitive?.content ?: "",
                lichtBehoefte = json["lichtBehoefte"]?.jsonPrimitive?.content ?: "",
                voedingAdvies = json["voedingAdvies"]?.jsonPrimitive?.content ?: "",
                ehboSignaal = json["ehboSignaal"]?.jsonPrimitive?.content ?: "",
                snoeiMaand = json["snoeiMaand"]?.jsonPrimitive?.content ?: "",
                snoeiAdvies = json["snoeiAdvies"]?.jsonPrimitive?.content ?: "",
                bemesting = json["bemesting"]?.jsonPrimitive?.content ?: ""
            )
        } catch (e: Exception) {
            println("Gemini Error: ${e.message}")
            AiPlantResult(
                naam = plantNaam, 
                omschrijving = wikipediaInfo ?: "Helaas kon er geen extra informatie worden opgehaald voor deze plant."
            )
        }
    }
}
