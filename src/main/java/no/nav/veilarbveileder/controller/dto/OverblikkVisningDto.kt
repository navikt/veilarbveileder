package no.nav.veilarbveileder.controller.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import jakarta.validation.constraints.NotNull
import no.nav.veilarbveileder.domain.OverblikkVisningAlternativ

data class OverblikkVisningRequest(
    @field:NotNull
    val overblikkVisning: List<@NotNull OverblikkVisningAlternativDto>
)

data class OverblikkVisningResponse(
    val overblikkVisning: List<OverblikkVisningAlternativDto>
)

enum class OverblikkVisningAlternativDto(
    @get:JsonValue val verdi: String
) {
    CV("CV"),
    JOBBØNSKER("Jobbønsker"),
    OPPFØLGING("Oppfølging"),
    PERSONALIA("Personalia"),
    REGISTRERING("Registrering"),
    YTELSER("Ytelser");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fraVerdi(verdi: String): OverblikkVisningAlternativDto =
            entries.firstOrNull { it.verdi == verdi }
                ?: throw IllegalArgumentException(
                    "Lovlige valg er CV, Jobbønsker, Oppfølging, Personalia, Registrering og Ytelser."
                )
    }
}

object OverblikkVisningDtoMapper {
    @JvmStatic
    fun tilDomene(request: OverblikkVisningRequest): List<OverblikkVisningAlternativ> =
        request.overblikkVisning.map(::tilDomene)

    @JvmStatic
    fun tilRespons(visning: List<OverblikkVisningAlternativ>): OverblikkVisningResponse =
        OverblikkVisningResponse(visning.map(::tilDto))

    @JvmStatic
    fun tilDomene(alternativ: OverblikkVisningAlternativDto): OverblikkVisningAlternativ = when (alternativ) {
        OverblikkVisningAlternativDto.CV -> OverblikkVisningAlternativ.CV
        OverblikkVisningAlternativDto.JOBBØNSKER -> OverblikkVisningAlternativ.JOBBØNSKER
        OverblikkVisningAlternativDto.OPPFØLGING -> OverblikkVisningAlternativ.OPPFØLGING
        OverblikkVisningAlternativDto.PERSONALIA -> OverblikkVisningAlternativ.PERSONALIA
        OverblikkVisningAlternativDto.REGISTRERING -> OverblikkVisningAlternativ.REGISTRERING
        OverblikkVisningAlternativDto.YTELSER -> OverblikkVisningAlternativ.YTELSER
    }

    @JvmStatic
    fun tilDto(alternativ: OverblikkVisningAlternativ): OverblikkVisningAlternativDto = when (alternativ) {
        OverblikkVisningAlternativ.CV -> OverblikkVisningAlternativDto.CV
        OverblikkVisningAlternativ.JOBBØNSKER -> OverblikkVisningAlternativDto.JOBBØNSKER
        OverblikkVisningAlternativ.OPPFØLGING -> OverblikkVisningAlternativDto.OPPFØLGING
        OverblikkVisningAlternativ.PERSONALIA -> OverblikkVisningAlternativDto.PERSONALIA
        OverblikkVisningAlternativ.REGISTRERING -> OverblikkVisningAlternativDto.REGISTRERING
        OverblikkVisningAlternativ.YTELSER -> OverblikkVisningAlternativDto.YTELSER
    }
}
