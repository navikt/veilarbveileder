package no.nav.veilarbveileder.service

import io.getunleash.DefaultUnleash
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import no.nav.common.auth.context.AuthContextHolder
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysEnhet
import no.nav.common.client.msgraph.AdGroupFilter
import no.nav.common.client.msgraph.MsGraphClient
import no.nav.common.client.norg2.Enhet
import no.nav.common.client.norg2.Norg2Client
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient
import no.nav.common.types.identer.EnhetId
import no.nav.common.types.identer.NavIdent
import no.nav.veilarbveileder.config.EnvironmentProperties
import no.nav.veilarbveileder.domain.PortefoljeEnhet
import no.nav.veilarbveileder.utils.BRUK_VEILEDERE_PAA_ENHET_FRA_AD
import no.nav.veilarbveileder.utils.HENT_ENHETER_FRA_AD_OG_LOGG_DIFF
import no.nav.veilarbveileder.utils.Mappers
import no.nav.veilarbveileder.utils.SecureLog
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
@RequiredArgsConstructor
@Slf4j
class EnhetService(
    private val norg2Client: Norg2Client,
    private val axsysClient: AxsysClient,
    private val msGraphClient: MsGraphClient,
    private val azureAdMachineToMachineTokenClient: AzureAdMachineToMachineTokenClient,
    private val azureAdOnBehalfOfTokenClient: AzureAdOnBehalfOfTokenClient,
    private val authContextHolder: AuthContextHolder,
    private val environmentProperties: EnvironmentProperties,
    private val defaultUnleash: DefaultUnleash
) {
    val logger = LoggerFactory.getLogger(javaClass)

    fun hentEnhet(enhetId: EnhetId): Optional<PortefoljeEnhet> {
        try {
            val enhet: Enhet? = norg2Client.hentEnhet(enhetId.get())
            return Optional.ofNullable(Mappers.tilPortefoljeEnhet(enhet))
        } catch (e: Exception) {
            logger.warn(String.format("Fant ikke enhet med id %s", enhetId), e)
            return Optional.empty()
        }
    }

    fun alleEnheter(): List<PortefoljeEnhet?> {
        return norg2Client.alleAktiveEnheter()
            .map { enhet: Enhet? -> Mappers.tilPortefoljeEnhet(enhet) }
    }

    fun veilederePaEnhet(enhetId: EnhetId?): List<NavIdent?>? {
        return if (defaultUnleash.isEnabled(BRUK_VEILEDERE_PAA_ENHET_FRA_AD)) {
            msGraphClient.hentUserDataForGroup(
                azureAdMachineToMachineTokenClient.createMachineToMachineToken(environmentProperties.microsoftGraphScope),
                enhetId
            ).map { NavIdent.of(it.onPremisesSamAccountName) }

        } else {
            axsysClient.hentAnsatte(enhetId)
        }
    }

    fun hentTilganger(navIdent: NavIdent): List<PortefoljeEnhet?> {
        return if (defaultUnleash.isEnabled(HENT_ENHETER_FRA_AD_OG_LOGG_DIFF)) {
            try {
                val aktiveEnheter = norg2Client.alleAktiveEnheter()
                val unikeEnhetTilgangerFraAxsys = hentEnheterFraAxsys(navIdent).toSet()
                val unikeEnhetTilgangerFraADGrupper = hentEnhetTilgangerFraADGrupper().map { enhetId ->
                    PortefoljeEnhet(
                        enhetId = enhetId,
                        navn = aktiveEnheter.firstOrNull { enhetId.get() == it.enhetNr }?.navn
                    )
                }.toSet()

                if (unikeEnhetTilgangerFraAxsys == unikeEnhetTilgangerFraADGrupper) {
                    logger.info("Enhettilganger er identiske mellom Axsys og AD-grupper.")
                } else {
                    val antallEnhetstilgangerAxsys = unikeEnhetTilgangerFraAxsys.size
                    val antallEnhetstilgangerADGrupper = unikeEnhetTilgangerFraADGrupper.size
                    logger.warn(
                        "Enhettilganger er ikke identiske mellom Axsys og AD-grupper. Antall enhetstilganger fra Axsys: {}, antall enhetstilganger fra AD-grupper: {}.",
                        antallEnhetstilgangerAxsys,
                        antallEnhetstilgangerADGrupper
                    )
                    SecureLog.secureLog.warn(
                        "Enhettilganger for ident {} er ikke identiske mellom Axsys og AD-grupper. ",
                        navIdent
                    )
                }

                unikeEnhetTilgangerFraADGrupper.toList()
            } catch (_: NavEnhetIdValideringException) {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        } else {
            hentEnheterFraAxsys(navIdent)
        }
    }

    fun hentEnheterFraAxsys(navIdent: NavIdent): List<PortefoljeEnhet> {
        return axsysClient.hentTilganger(navIdent)
            .map { axsysEnhet: AxsysEnhet? -> Mappers.tilPortefoljeEnhet(axsysEnhet) }
    }

    fun hentEnhetTilgangerFraADGrupper(): Set<EnhetId> {
        return msGraphClient.hentAdGroupsForUser(
            azureAdOnBehalfOfTokenClient.exchangeOnBehalfOfToken(
                environmentProperties.microsoftGraphScope,
                authContextHolder.requireIdTokenString()
            ),
            AdGroupFilter.ENHET
        )
            .map { tilEnhetId(it.displayName) }
            .toSet()
    }

    companion object {
        internal const val AD_GRUPPE_ENHET_PREFIKS = "0000-GA-ENHET_"
        internal const val NAV_ENHET_ID_LENGDE = 4

        fun tilEnhetId(adGruppeNavn: String): EnhetId {
            return adGruppeNavn
                .uppercase()
                .substringAfter(AD_GRUPPE_ENHET_PREFIKS)
                .let(::tilValidertEnhetId)
        }

        private fun tilValidertEnhetId(navEnhetId: String): EnhetId {
            if (navEnhetId.length != NAV_ENHET_ID_LENGDE) throw NavEnhetIdValideringException("Ugyldig lengde: ${navEnhetId.length}. Forventet: $NAV_ENHET_ID_LENGDE.")
            if (
                !navEnhetId.all { it.isDigit() }
            ) throw NavEnhetIdValideringException("Ugyldige tegn: ${navEnhetId}. Forventet: 4 siffer.")

            return EnhetId.of(navEnhetId)
        }
    }

    internal data class NavEnhetIdValideringException(val melding: String) : RuntimeException(melding)
}
