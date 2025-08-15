package no.nav.veilarbveileder.service

import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysEnhet
import no.nav.common.client.norg2.Enhet
import no.nav.common.client.norg2.Norg2Client
import no.nav.common.types.identer.EnhetId
import no.nav.common.types.identer.NavIdent
import no.nav.veilarbveileder.domain.PortefoljeEnhet
import no.nav.veilarbveileder.utils.Mappers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import java.util.stream.Collectors

@Service
@RequiredArgsConstructor
@Slf4j
class EnhetService {
    val logger = LoggerFactory.getLogger(javaClass)

    private val norg2Client: Norg2Client? = null

    private val axsysClient: AxsysClient? = null

    fun hentEnhet(enhetId: EnhetId): Optional<PortefoljeEnhet> {
        try {
            val enhet = norg2Client!!.hentEnhet(enhetId.get())
            return Optional.of(Mappers.tilPortefoljeEnhet(enhet))
        } catch (e: Exception) {
            logger.warn(String.format("Fant ikke enhet med id %s", enhetId), e)
            return Optional.empty()
        }
    }

    fun alleEnheter(): MutableList<PortefoljeEnhet?> {
        return norg2Client!!.alleAktiveEnheter()
            .stream()
            .map { enhet: Enhet? -> Mappers.tilPortefoljeEnhet(enhet) }
            .collect(Collectors.toList())
    }

    fun veilederePaEnhet(enhetId: EnhetId?): MutableList<NavIdent?>? {
        return axsysClient!!.hentAnsatte(enhetId)
    }

    fun hentTilganger(navIdent: NavIdent?): MutableList<PortefoljeEnhet?> {
        return axsysClient!!.hentTilganger(navIdent)
            .stream()
            .map { axsysEnhet: AxsysEnhet? -> Mappers.tilPortefoljeEnhet(axsysEnhet) }
            .collect(Collectors.toList())
    }
}

