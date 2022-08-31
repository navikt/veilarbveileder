package no.nav.veilarbveileder.controller.dto

import no.nav.common.types.identer.NavIdent

data class ListVeiledereRequest(
    val identer: List<NavIdent>
)