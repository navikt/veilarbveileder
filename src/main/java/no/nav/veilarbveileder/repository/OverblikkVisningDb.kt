package no.nav.veilarbveileder.repository

import no.nav.veilarbveileder.domain.OverblikkVisning
import no.nav.veilarbveileder.domain.OverblikkVisningAlternativ
import java.util.UUID

internal object OverblikkVisningTabell {
    const val NAVN = "OVERBLIKK_VISNING"
    const val ID = "OVERBLIKK_VISNING_ID"
    const val VEILEDER_ID = "VEILEDER_ID"
    const val VISNING = "VISNING"
    const val OPPRETTET_TIDSPUNKT = "OPPRETTET_TIDSPUNKT"
    const val SIST_ENDRET_TIDSPUNKT = "SIST_ENDRET_TIDSPUNKT"
}

internal data class OverblikkVisningDb(
    val id: UUID,
    val visning: List<OverblikkVisningAlternativDb>
)

internal enum class OverblikkVisningAlternativDb {
    CV,
    JOBBØNSKER,
    OPPFØLGING,
    PERSONALIA,
    REGISTRERING,
    YTELSER
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
