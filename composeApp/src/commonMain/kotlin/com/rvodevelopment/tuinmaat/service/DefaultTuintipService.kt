package com.rvodevelopment.tuinmaat.service

class DefaultTuintipService : TuintipService {
    private val tips = listOf(
        "Geef je planten bij voorkeur 's ochtends vroeg water, zo verdampt er minder.",
        "Snoei uitgebloeide bloemen weg om een tweede bloei te stimuleren.",
        "Mulch de bodem om vocht vast te houden en onkruid te onderdrukken.",
        "Controleer regelmatig op luis en andere plagen.",
        "Gebruik regenwater voor je kamerplanten, dat bevat minder kalk."
    )

    override suspend fun getTuintip(): Result<String> {
        return Result.success(tips.random())
    }
}
