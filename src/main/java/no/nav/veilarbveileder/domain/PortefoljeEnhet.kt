package no.nav.veilarbveileder.domain

import no.nav.common.types.identer.EnhetId

data class PortefoljeEnhet(
    val enhetId: EnhetId,
    val navn: String? = null
)
