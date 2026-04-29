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
            
            // Probeer Wikipedia met de gewone naam, dan met de wetenschappelijke naam
            var wikipediaInfo = haalWikipediaInfoOp(plantNaam)
            if (wikipediaInfo == null && scientificName.isNotEmpty() && scientificName != plantNaam) {
                wikipediaInfo = haalWikipediaInfoOp(scientificName)
            }
            
            val finaleInfo = verrijkMetGemini(plantNaam, scientificName, wikipediaInfo)
            val bron = if (wikipediaInfo != null) "Pl@ntNet • Wikipedia • Gemini AI" else "Pl@ntNet • Gemini AI"
            
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
                    append("organs", "leaf")
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
                
                // Zoek naar een Nederlandse naam
                val commonNames = species?.get("commonNames")?.jsonArray
                val commonName = if (commonNames != null && commonNames.isNotEmpty()) {
                    commonNames[0].jsonPrimitive.content
                } else {
                    scientificName
                }
                Pair(commonName, scientificName)
            } else null
        } catch (e: Exception) {
            println("PlantNet Error: ${e.message}")
            null
        }
    }

    private suspend fun haalWikipediaInfoOp(naam: String): String? {
        return try {
            val encodedNaam = naam.replace(" ", "_")
            // Eerst direct proberen via de summary API
            val response: JsonObject = client.get("https://nl.wikipedia.org/api/rest_v1/page/summary/$encodedNaam") {
                accept(ContentType.Application.Json)
            }.body()
            
            val extract = response["extract"]?.jsonPrimitive?.content
            if (!extract.isNullOrBlank()) return extract

            // Als dat faalt, probeer de search API om de juiste titel te vinden
            val searchResponse: JsonObject = client.get("https://nl.wikipedia.org/w/api.php") {
                parameter("action", "query")
                parameter("list", "search")
                parameter("srsearch", naam)
                parameter("format", "json")
                parameter("origin", "*")
            }.body()

            val searchResults = searchResponse["query"]?.jsonObject?.get("search")?.jsonArray
            if (searchResults != null && searchResults.isNotEmpty()) {
                val bestTitle = searchResults[0].jsonObject["title"]?.jsonPrimitive?.content
                if (bestTitle != null) {
                    val encodedTitle = bestTitle.replace(" ", "_")
                    val summaryResponse: JsonObject = client.get("https://nl.wikipedia.org/api/rest_v1/page/summary/$encodedTitle") {
                        accept(ContentType.Application.Json)
                    }.body()
                    return summaryResponse["extract"]?.jsonPrimitive?.content
                }
            }
            null
        } catch (e: Exception) {
            println("Wikipedia Error for $naam: ${e.message}")
            null
        }
    }

    private suspend fun verrijkMetGemini(plantNaam: String, scientificName: String, wikipediaInfo: String?): AiPlantResult {
        val wikiText = wikipediaInfo ?: "Geen extra Wikipedia informatie beschikbaar. Gebruik je eigen kennis."
        val prompt = """
            Je bent een expert hovenier met diepgaande kennis van botanie en plantenverzorging.
            Geef gedetailleerde verzorgingsinformatie voor de plant: $plantNaam ($scientificName).
            Wikipedia context: $wikiText
            
            Vul alle onderstaande velden in het Nederlands in voor een tuinier-app. 
            Zorg dat ELK veld zinvol en volledig is ingevuld.
            
            Velden uitleg:
            1. omschrijving: Een boeiende tekst over de plant, uiterlijk, herkomst en waarom hij leuk is (max 60 woorden).
            2. waterBehoefte: Hoe vaak en hoeveel water? Specifiek advies.
            3. lichtBehoefte: De ideale standplaats (bijv. volle zon, halfschaduw, schaduw).
            4. voedingAdvies: Wanneer en hoe vaak heeft de plant extra plantenvoeding nodig?
            5. bemesting: Welke specifieke soort meststof of bodemverbeteraar is het best?
            6. snoeiMaand: De BESTE maand om te snoeien. Geef slechts ÉÉN maand. Gebruik uitsluitend een van deze namen: Januari, Februari, Maart, April, Mei, Juni, Juli, Augustus, September, Oktober, November, December. Laat leeg als snoeien niet nodig is.
            7. snoeiAdvies: Korte, praktische instructies over de techniek van het snoeien (max 30 woorden).
            8. ehboSignaal: Hoe zie je dat de plant ongezond is? (bijv. bruine randen, slap hangen, gele bladeren).

            ANTWOORD UITSLUITEND IN DIT JSON FORMAAT:
            {
              "omschrijving": "...",
              "waterBehoefte": "...",
              "lichtBehoefte": "...",
              "voedingAdvies": "...",
              "bemesting": "...",
              "snoeiMaand": "...",
              "snoeiAdvies": "...",
              "ehboSignaal": "..."
            }
        """.trimIndent()

        return try {
            // Gebruik gemini-2.5-flash-lite op verzoek van de gebruiker voor de beste resultaten
            val response: JsonObject = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=$geminiApiKey") {
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
                    putJsonObject("generationConfig") {
                        put("response_mime_type", "application/json")
                    }
                })
            }.body()

            val textResult = response["candidates"]?.jsonArray?.get(0)?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray?.get(0)?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: "{}"

            val json = Json { ignoreUnknownKeys = true }.parseToJsonElement(textResult).jsonObject

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
