package no.nav.veilarbveileder.repository

import no.nav.veilarbveileder.domain.OverblikkVisning
import no.nav.veilarbveileder.domain.OverblikkVisningAlternativ
import java.util.UUID

internal data class OverblikkVisningDb(
    val id: UUID,
    val visning: List<OverblikkVisningAlternativDb>
)

internal enum class OverblikkVisningAlternativDb(
    private val tidligereVerdi: String
) {
    CV("CV"),
    JOBBØNSKER("Jobbønsker"),
    OPPFØLGING("Oppfølging"),
    PERSONALIA("Personalia"),
    REGISTRERING("Registrering"),
    YTELSER("Ytelser");

    companion object {
        fun fraVerdi(verdi: String): OverblikkVisningAlternativDb =
            entries.firstOrNull { it.name == verdi || it.tidligereVerdi == verdi }
                ?: throw IllegalArgumentException("Ugyldig overblikkvisning i databasen: $verdi")
    }
}

internal object OverblikkVisningDbMapper {
    @JvmStatic
    fun tilDomene(visning: OverblikkVisningDb): OverblikkVisning =
        OverblikkVisning(visning.id, visning.visning.map(::tilDomene))

    @JvmStatic
    fun tilDomene(alternativ: OverblikkVisningAlternativDb): OverblikkVisningAlternativ = when (alternativ) {
        OverblikkVisningAlternativDb.CV -> OverblikkVisningAlternativ.CV
        OverblikkVisningAlternativDb.JOBBØNSKER -> OverblikkVisningAlternativ.JOBBØNSKER
        OverblikkVisningAlternativDb.OPPFØLGING -> OverblikkVisningAlternativ.OPPFØLGING
        OverblikkVisningAlternativDb.PERSONALIA -> OverblikkVisningAlternativ.PERSONALIA
        OverblikkVisningAlternativDb.REGISTRERING -> OverblikkVisningAlternativ.REGISTRERING
        OverblikkVisningAlternativDb.YTELSER -> OverblikkVisningAlternativ.YTELSER
    }

    @JvmStatic
    fun tilDb(alternativ: OverblikkVisningAlternativ): OverblikkVisningAlternativDb = when (alternativ) {
        OverblikkVisningAlternativ.CV -> OverblikkVisningAlternativDb.CV
        OverblikkVisningAlternativ.JOBBØNSKER -> OverblikkVisningAlternativDb.JOBBØNSKER
        OverblikkVisningAlternativ.OPPFØLGING -> OverblikkVisningAlternativDb.OPPFØLGING
        OverblikkVisningAlternativ.PERSONALIA -> OverblikkVisningAlternativDb.PERSONALIA
        OverblikkVisningAlternativ.REGISTRERING -> OverblikkVisningAlternativDb.REGISTRERING
        OverblikkVisningAlternativ.YTELSER -> OverblikkVisningAlternativDb.YTELSER
    }
}
