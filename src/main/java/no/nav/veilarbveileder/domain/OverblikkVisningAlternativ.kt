package no.nav.veilarbveileder.domain

enum class OverblikkVisningAlternativ {
    CV,
    JOBBØNSKER,
    REGISTRERING,
    OPPFØLGING,
    YTELSER,
    PERSONALIA;

    companion object {
        fun erGyldig(verdi: String): Boolean = entries.any { it.name == verdi.uppercase() }
    }
}
