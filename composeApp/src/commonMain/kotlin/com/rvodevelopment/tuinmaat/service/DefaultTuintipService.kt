package com.rvodevelopment.tuinmaat.service

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DefaultTuintipService(
    private val aiService: AiService? = null
) : TuintipService {
    
    private val tipsPerMaand = mapOf(
        1 to listOf(
            "Controleer je tuin op vorstschade aan planten.",
            "Maak je tuingereedschap grondig schoon en vet het in.",
            "Plan je moestuin voor het komende jaar.",
            "Geef kamerplanten minder water, ze rusten nu."
        ),
        2 to listOf(
            "Snoei fruitbomen en struiken als het niet vriest.",
            "Zaai de eerste vroege groenten binnen voor.",
            "Verwijder mos en algen van je terras.",
            "Hang nestkastjes op voor de vogels."
        ),
        3 to listOf(
            "Snoei rozen en lavendel aan het einde van de maand.",
            "Bemest de border voor een goede start van het seizoen.",
            "Begin met het wieden van onkruid zodra het opkomt.",
            "Verdeel vaste planten die te groot zijn geworden."
        ),
        4 to listOf(
            "Zaai eenjarigen direct in de volle grond.",
            "Maai het gazon voor de eerste keer (op een hoge stand).",
            "Steun hoge planten alvast met plantensteunen.",
            "Bestrijd slakken op een milieuvriendelijke manier."
        ),
        5 to listOf(
            "Zet eenjarige zomerbloeiers buiten na de IJsheiligen.",
            "Snoei voorjaarsbloeiende struiken na de bloei.",
            "Geef pas geplante planten regelmatig water.",
            "Controleer op luis op jonge scheuten."
        ),
        6 to listOf(
            "Knip uitgebloeide bloemen weg voor een tweede bloei.",
            "Sproei bij droogte bij voorkeur in de vroege ochtend.",
            "Knip hagen zoals de buxus of liguster.",
            "Dun vruchten aan fruitbomen uit voor grotere oogst."
        ),
        7 to listOf(
            "Geef planten in potten dagelijks water bij warm weer.",
            "Oogst regelmatig groenten en fruit uit de moestuin.",
            "Verwijder onkruid voordat het zaad gaat vormen.",
            "Maai het gazon niet te kort bij aanhoudende droogte."
        ),
        8 to listOf(
            "Blijf uitgebloeide bloemen verwijderen.",
            "Neem stekken van je favoriete planten.",
            "Snoei klimplanten zoals de blauwe regen.",
            "Bestel alvast bollen voor het najaar."
        ),
        9 to listOf(
            "Begin met het planten van voorjaarsbollen.",
            "Verzamel zaden van uitgebloeide bloemen.",
            "Breng een mulchlaag aan om de bodem te verbeteren.",
            "Maak de vijver schoon en verwijder afgevallen blad."
        ),
        10 to listOf(
            "Plant bomen, struiken en hagen.",
            "Haal niet-winterharde knollen (zoals dahlia's) naar binnen.",
            "Verwijder afgestorven plantenresten, maar laat wat liggen voor insecten.",
            "Maak je tuin winterklaar."
        ),
        11 to listOf(
            "Bescherm vorstgevoelige planten met vliesdoek of stro.",
            "Plant de laatste voorjaarsbollen (tulpen).",
            "Laat uitgebloeide siergrassen staan voor een mooi wintersilhouet.",
            "Maak de buitenkraan leeg en sluit deze af."
        ),
        12 to listOf(
            "Controleer de winterbescherming bij harde wind of vorst.",
            "Geniet van de rust in de tuin en de vogels.",
            "Snoei druiven voor de kortste dag van het jaar.",
            "Maak plannen voor aanpassingen in de tuin volgend jaar."
        )
    )

    override suspend fun getTuintips(): Result<List<String>> {
        val nu = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val maand = nu.monthNumber
        return Result.success(tipsPerMaand[maand] ?: listOf("Geniet van je tuin!"))
    }

    override suspend fun getWeerBericht(): Result<WeerBericht> {
        // In een echte app zou dit van een API komen. Voor nu mocken we het.
        // We kunnen de huidige maand gebruiken om een logisch weertype te kiezen.
        val nu = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val maand = nu.monthNumber
        
        val bericht = when (maand) {
            in 3..5 -> WeerBericht(15, "Zonnig", "Sunny", "Het is prachtig weer om te zaaien of het gazon te maaien!")
            in 6..8 -> WeerBericht(24, "Heet en Droog", "Sunny", "Het blijft droog deze week, zorg voor voldoende water in de tuin.")
            in 9..11 -> WeerBericht(12, "Licht Bewolkt", "Cloudy", "Prima weer om bollen te planten of bladeren te harken.")
            else -> WeerBericht(4, "Koud en Nat", "Rain", "Blijf lekker binnen en maak plannen voor het voorjaar.")
        }
        
        return Result.success(bericht)
    }

    override suspend fun getActueelTuintip(plantNames: List<String>): Result<WeerBericht> {
        return aiService?.generateGardenTip(plantNames)?.map { aiTip ->
            WeerBericht(
                temperatuur = aiTip.temperatuur,
                conditie = aiTip.conditie,
                icoon = aiTip.icoon,
                advies = aiTip.tip
            )
        } ?: getWeerBericht() // Fallback naar statisch als AI niet beschikbaar is
    }
}
