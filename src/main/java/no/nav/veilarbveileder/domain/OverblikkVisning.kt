package no.nav.veilarbveileder.domain

import java.util.UUID

data class OverblikkVisning(
    val id: UUID,
    val visning: List<String>
)
