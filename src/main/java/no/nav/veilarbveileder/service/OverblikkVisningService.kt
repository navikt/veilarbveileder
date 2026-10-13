package no.nav.veilarbveileder.service

import no.nav.common.types.identer.NavIdent
import no.nav.veilarbveileder.domain.OverblikkVisningAlternativ
import no.nav.veilarbveileder.repository.OverblikkVisningRepository
import org.springframework.stereotype.Service

@Service
class OverblikkVisningService(private val overblikkVisningRepository: OverblikkVisningRepository) {

    fun hent(veilederId: NavIdent): List<String> = overblikkVisningRepository.hent(veilederId).orEmpty()

    fun lagre(veilederId: NavIdent, visning: List<String>) {
        require(visning.all(OverblikkVisningAlternativ::erGyldig)) { "Ugyldige verdier i overblikkvisning: $visning" }
        overblikkVisningRepository.lagre(veilederId, visning)
    }

    fun slett(veilederId: NavIdent) = overblikkVisningRepository.slett(veilederId)
}
