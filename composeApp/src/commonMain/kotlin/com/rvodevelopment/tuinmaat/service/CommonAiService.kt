package com.rvodevelopment.tuinmaat.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.json.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CommonAiService(
    private val client: HttpClient,
    private val plantnetApiKey: String,
    private val geminiApiKey: String,
    private val geminiModel: String,
    private val mediaService: MediaService
) : AiService {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun identifyPlant(imageBytes: ByteArray): Result<AiPlantResult> = withContext(Dispatchers.IO) {
        val start = Clock.System.now()
        try {
            // Stap 1: Resize & PlantNet (Sequential want PlantNet geeft de naam)
            val plantnetImage = mediaService.resizeImage(imageBytes, 1280)
            val plantNetResult = identificeerMetPlantNet(plantnetImage)
            val plantNetDone = Clock.System.now()
            
            val plantNaam = plantNetResult?.first ?: return@withContext Result.failure(Exception("Pl@ntNet kon de plant niet identificeren"))
            val scientificName = plantNetResult.second
            println("TIMING: PlantNet identificeerde '$plantNaam' in ${plantNetDone.toEpochMilliseconds() - start.toEpochMilliseconds()}ms")
            
            // Stap 2 & 3: Wikipedia en Gemini PARALLEL starten
            val (wikipediaInfo, finaleInfo) = coroutineScope {
                val wiki = async { haalWikipediaInfoOp(plantNaam) }
                val gem = async { verrijkMetGemini(plantNaam, scientificName) }
                Pair(wiki.await(), gem.await())
            }
            val totalDone = Clock.System.now()
            println("TIMING: Parallelle taken klaar na ${totalDone.toEpochMilliseconds() - plantNetDone.toEpochMilliseconds()}ms")
            
            val bron = if (wikipediaInfo != null) "Pl@ntNet • Wikipedia • Gemini AI" else "Pl@ntNet • Gemini AI"
            
            Result.success(finaleInfo.copy(
                naam = plantNaam,
                bron = bron
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun identifyPlantByName(name: String): Result<AiPlantResult> = withContext(Dispatchers.IO) {
        val start = Clock.System.now()
        try {
            // Wikipedia en Gemini PARALLEL
            val (wikipediaInfo, finaleInfo) = coroutineScope {
                val wiki = async { haalWikipediaInfoOp(name) }
                val gem = async { verrijkMetGemini(name, "") }
                Pair(wiki.await(), gem.await())
            }
            
            val geminiDone = Clock.System.now()
            println("TIMING: Totaal (by name parallel) duurde ${geminiDone.toEpochMilliseconds() - start.toEpochMilliseconds()}ms")

            val bron = if (wikipediaInfo != null) "Wikipedia • Gemini AI" else "Gemini AI"
            
            Result.success(finaleInfo.copy(
                naam = name,
                bron = bron
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun identifyDisease(imageBytes: ByteArray, plantName: String?): Result<AiDiseaseResult> = withContext(Dispatchers.IO) {
        try {
            val resizedImage = mediaService.resizeImage(imageBytes, 1024)
            val plantNetDisease = identificeerZiekteMetPlantNet(resizedImage)
            
            val eppoCode = plantNetDisease?.first ?: ""
            val diseaseName = plantNetDisease?.second ?: "Onbekende aandoening"
            val referencePhoto = plantNetDisease?.third

            val geminiResult = verrijkZiekteMetGemini(diseaseName, plantName ?: "onbekende plant")
            
            Result.success(geminiResult.copy(
                ziekteNaam = diseaseName,
                eppoCode = eppoCode,
                referentieFoto = referencePhoto
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

    private suspend fun identificeerZiekteMetPlantNet(imageBytes: ByteArray): Triple<String, String, String?>? {
        return try {
            val response: String = client.submitFormWithBinaryData(
                url = "https://my-api.plantnet.org/v2/diseases/identify?api-key=$plantnetApiKey&lang=nl&include-related-images=true",
                formData = formData {
                    append("images", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"disease.jpg\"")
                    })
                }
            ).body()

            val json = Json.parseToJsonElement(response).jsonObject
            val results = json["results"]?.jsonArray
            if (results != null && results.isNotEmpty()) {
                val bestMatch = results[0].jsonObject
                val eppoCode = bestMatch["name"]?.jsonPrimitive?.content ?: ""
                val description = bestMatch["description"]?.jsonPrimitive?.content ?: eppoCode
                
                val referenceImage = bestMatch["images"]?.jsonArray?.getOrNull(0)?.jsonObject
                    ?.get("url")?.jsonObject?.get("m")?.jsonPrimitive?.content
                
                Triple(eppoCode, description, referenceImage)
            } else null
        } catch (e: Exception) {
            println("PlantNet Disease Error: ${e.message}")
            null
        }
    }

    private suspend fun verrijkZiekteMetGemini(
        ziekteNaam: String,
        plantNaam: String
    ): AiDiseaseResult {
        val prompt = """
            Ziekte/Aandoening: $ziekteNaam
            Plant: $plantNaam
            
            Voer een HOLISTISCHE analyse uit van deze plant. Gebruik de foto-informatie om een totaalbeeld te vormen.
            Kijk naar:
            1. ZIEKTES & PLAGEN: Identificeer specifieke infecties of insecten.
            2. WATERHUISHOUDING: Zie je tekenen van wortelrot (te nat) of verwelking/bruine randen (te droog)?
            3. LICHT & STANDPLAATS: Zie je verbranding (te veel zon) of spichtige groei/geel blad (te weinig licht)?
            4. VOEDINGSSTOFFEN: Zie je verkleuringen die duiden op tekorten (bv. stikstof, magnesium).
            
            Geef de resultaten in JSON:
            {
              "omschrijving": "Een samenvatting van de algehele conditie van de plant (max 40 woorden)",
              "advies": "Een concreet actieplan om de plant weer gezond te krijgen (max 60 woorden)",
              "waterAnalyse": "Status bewatering (bv: 'Te nat - kans op rot', 'Te droog' of 'Goed')",
              "lichtAnalyse": "Status licht (bv: 'Te veel direct zonlicht', 'Te donker' of 'Ideaal')",
              "voedingAnalyse": "Status voeding (bv: 'Stikstoftekort', 'Overbemest' of 'Voldoende')"
            }
            Antwoord uitsluitend in het Nederlands.
        """.trimIndent()

        return try {
            val response: JsonObject = client.post("https://generativelanguage.googleapis.com/v1beta/models/$geminiModel:generateContent?key=$geminiApiKey") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    putJsonArray("contents") {
                        addJsonObject {
                            putJsonArray("parts") {
                                addJsonObject { put("text", prompt) }
                            }
                        }
                    }
                    putJsonObject("system_instruction") {
                        putJsonArray("parts") {
                            addJsonObject { put("text", "Je bent een expert plantendokter. Je analyseert zowel ziektes als verzorgingsfouten (water, licht, voeding). Antwoord uitsluitend in valide JSON.") }
                        }
                    }
                    putJsonObject("generationConfig") {
                        put("response_mime_type", "application/json")
                        put("temperature", 0.1)
                    }
                })
            }.body()

            val textResult = response["candidates"]?.jsonArray?.get(0)?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray?.get(0)?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: "{}"

            val parsedJson = json.parseToJsonElement(textResult).jsonObject

            AiDiseaseResult(
                ziekteNaam = ziekteNaam,
                score = 0.0,
                eppoCode = "",
                omschrijving = parsedJson["omschrijving"]?.jsonPrimitive?.content ?: "",
                advies = parsedJson["advies"]?.jsonPrimitive?.content ?: "",
                waterAnalyse = parsedJson["waterAnalyse"]?.jsonPrimitive?.content ?: "Geen afwijkingen",
                lichtAnalyse = parsedJson["lichtAnalyse"]?.jsonPrimitive?.content ?: "Geen afwijkingen",
                voedingAnalyse = parsedJson["voedingAnalyse"]?.jsonPrimitive?.content ?: "Geen afwijkingen"
            )
        } catch (e: Exception) {
            println("Gemini Disease Error: ${e.message}")
            AiDiseaseResult(
                ziekteNaam = ziekteNaam,
                score = 0.0,
                eppoCode = "",
                omschrijving = "Informatie kon niet worden opgehaald.",
                advies = "Raadpleeg een expert als de symptomen verergeren."
            )
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

    private suspend fun verrijkMetGemini(
        plantNaam: String, 
        scientificName: String
    ): AiPlantResult {
        val prompt = """
            Plant: $plantNaam
            Wetenschappelijke naam: $scientificName
            
            Geef verzorgingsinformatie in JSON:
            {
              "wetenschappelijkeNaam": "Latijnse naam",
              "omschrijving": "Korte tekst (max 40 woorden)",
              "waterBehoefte": "Wateradvies",
              "lichtBehoefte": "Standplaats",
              "voedingAdvies": "Voedingsbehoefte",
              "bemesting": "Grond/Mestadvies",
              "snoeiMaand": "Snoeimaand (Januari...December of leeg)",
              "snoeiAdvies": "Snoei-instructie (max 20 woorden)",
              "ehboSignaal": "Symptomen ongezond"
            }
            Taal: Nederlands.
        """.trimIndent()

        return try {
            val response: JsonObject = client.post("https://generativelanguage.googleapis.com/v1beta/models/$geminiModel:generateContent?key=$geminiApiKey") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    putJsonArray("contents") {
                        addJsonObject {
                            putJsonArray("parts") {
                                addJsonObject { put("text", prompt) }
                            }
                        }
                    }
                    putJsonObject("system_instruction") {
                        putJsonArray("parts") {
                            addJsonObject { put("text", "Je bent een expert hovenier. Antwoord uitsluitend in valide JSON.") }
                        }
                    }
                    putJsonObject("generationConfig") {
                        put("response_mime_type", "application/json")
                        put("temperature", 0.1)
                        put("max_output_tokens", 600)
                    }
                })
            }.body()

            val textResult = response["candidates"]?.jsonArray?.get(0)?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray?.get(0)?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: "{}"

            val parsedJson = json.parseToJsonElement(textResult).jsonObject

            AiPlantResult(
                naam = plantNaam,
                wetenschappelijkeNaam = parsedJson["wetenschappelijkeNaam"]?.jsonPrimitive?.content ?: scientificName,
                omschrijving = parsedJson["omschrijving"]?.jsonPrimitive?.content ?: "",
                waterBehoefte = parsedJson["waterBehoefte"]?.jsonPrimitive?.content ?: "",
                lichtBehoefte = parsedJson["lichtBehoefte"]?.jsonPrimitive?.content ?: "",
                voedingAdvies = parsedJson["voedingAdvies"]?.jsonPrimitive?.content ?: "",
                ehboSignaal = parsedJson["ehboSignaal"]?.jsonPrimitive?.content ?: "",
                snoeiMaand = parsedJson["snoeiMaand"]?.jsonPrimitive?.content ?: "",
                snoeiAdvies = parsedJson["snoeiAdvies"]?.jsonPrimitive?.content ?: "",
                bemesting = parsedJson["bemesting"]?.jsonPrimitive?.content ?: ""
            )
        } catch (e: Exception) {
            println("Gemini Error: ${e.message}")
            AiPlantResult(
                naam = plantNaam, 
                omschrijving = "Informatie kon niet worden opgehaald."
            )
        }
    }

    override suspend fun generateGardenTip(plantNames: List<String>): Result<AiGardenTip> = withContext(Dispatchers.IO) {
        try {
            // Stap 1: Haal actueel weer op (Open-Meteo, geen API key nodig)
            val weatherResponse: JsonObject = client.get("https://api.open-meteo.com/v1/forecast?latitude=52.1326&longitude=5.2913&current_weather=true").body()
            val current = weatherResponse["current_weather"]?.jsonObject
            val temp = current?.get("temperature")?.jsonPrimitive?.content?.toDoubleOrNull()?.toInt() ?: 15
            val weatherCode = current?.get("weathercode")?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            
            val conditie = when (weatherCode) {
                0 -> "Zonnig"
                1, 2, 3 -> "Licht bewolkt"
                45, 48 -> "Mistig"
                51, 53, 55, 61, 63, 65 -> "Regenachtig"
                71, 73, 75 -> "Sneeuw"
                95, 96, 99 -> "Onweer"
                else -> "Wisselvallig"
            }
            
            val icoon = when (weatherCode) {
                0, 1 -> "Sunny"
                2, 3 -> "Cloudy"
                51, 53, 55, 61, 63, 65, 80, 81, 82 -> "Rain"
                else -> "Cloudy"
            }

            // Stap 2: Genereer tip met Gemini
            val nu = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val maandNaam = when(nu.monthNumber) {
                1 -> "Januari"
                2 -> "Februari"
                3 -> "Maart"
                4 -> "April"
                5 -> "Mei"
                6 -> "Juni"
                7 -> "Juli"
                8 -> "Augustus"
                9 -> "September"
                10 -> "Oktober"
                11 -> "November"
                12 -> "December"
                else -> "deze maand"
            }

            val plantContext = if (plantNames.isNotEmpty()) {
                "De gebruiker heeft de volgende planten in de tuin: ${plantNames.joinToString(", ")}. "
            } else ""

            val prompt = """
                Maand: $maandNaam
                Weer: $conditie, $temp graden.
                Planten in tuin: ${plantNames.joinToString(", ")}
                Geef een korte tuintip (max 2 zinnen).
            """.trimIndent()

            val response: JsonObject = client.post("https://generativelanguage.googleapis.com/v1beta/models/$geminiModel:generateContent?key=$geminiApiKey") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    putJsonArray("contents") {
                        addJsonObject {
                            putJsonArray("parts") {
                                addJsonObject { put("text", prompt) }
                            }
                        }
                    }
                    putJsonObject("system_instruction") {
                        putJsonArray("parts") {
                            addJsonObject { put("text", "Je bent een expert hovenier. Antwoord uitsluitend in JSON: {\"tip\": \"...\"}") }
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

            val parsedJson = json.parseToJsonElement(textResult).jsonObject
            val tip = parsedJson["tip"]?.jsonPrimitive?.content ?: "Geniet van je tuin vandaag!"

            Result.success(AiGardenTip(temp, conditie, icoon, tip))
        } catch (e: Exception) {
            println("Garden Tip Error: ${e.message}")
            Result.failure(e)
        }
    }
}
