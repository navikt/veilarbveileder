package no.nav.veilarbveileder.service

import no.nav.common.types.identer.NavIdent
import no.nav.veilarbveileder.domain.OverblikkVisningAlternativ
import no.nav.veilarbveileder.repository.OverblikkVisningRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OverblikkVisningService(private val overblikkVisningRepository: OverblikkVisningRepository) {

    fun hent(veilederId: NavIdent): List<OverblikkVisningAlternativ> =
        overblikkVisningRepository.hent(veilederId)?.visning.orEmpty()

    @Transactional
    fun lagre(veilederId: NavIdent, visning: List<OverblikkVisningAlternativ>) {
        overblikkVisningRepository.lagre(veilederId, visning)
    }

    fun slett(veilederId: NavIdent) = overblikkVisningRepository.slett(veilederId)
}
