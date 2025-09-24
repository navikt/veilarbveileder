package no.nav.veilarbveileder.service

import io.getunleash.DefaultUnleash
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysEnhet
import no.nav.common.client.msgraph.AdGroupData
import no.nav.common.client.msgraph.MsGraphClient
import no.nav.common.client.norg2.Enhet
import no.nav.common.client.norg2.Norg2Client
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient
import no.nav.common.types.identer.EnhetId
import no.nav.common.types.identer.NavIdent
import no.nav.veilarbveileder.client.MicrosoftGraphClient
import no.nav.veilarbveileder.config.EnvironmentProperties
import no.nav.veilarbveileder.domain.PortefoljeEnhet
import no.nav.veilarbveileder.utils.BRUK_VEILEDERE_PAA_ENHET_FRA_AD
import no.nav.veilarbveileder.utils.HENT_ENHETER_FRA_AD_OG_LOGG_DIFF
import no.nav.veilarbveileder.utils.Mappers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
@RequiredArgsConstructor
@Slf4j
class EnhetService(
    private val norg2Client: Norg2Client,
    private val axsysClient: AxsysClient,
    private val microsoftGraphClient: MicrosoftGraphClient,
    private val msGraphClient: MsGraphClient,
    private val azureAdMachineToMachineTokenClient: AzureAdMachineToMachineTokenClient,
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
            val enhetTilgangerFraAxsys = hentEnheterFraAxsys(navIdent)

            // 2025-08-15: Her sammenlignes og logges bare eventuell differanse mellom gammel (Axsys) og ny
            // (vha. AD-grupper) måte å hente enhetstilganger på. Axsys er fortsatt "fasit".
            try {
                val unikeEnhetTilgangerFraADGrupper = hentEnhetTilgangerFraADGrupper()
                val adGrupperForNavIdent = hentTilgangerFraEntraId(navIdent)
                val unikeEnhetTilgangerFraAxsys =
                    enhetTilgangerFraAxsys.map(PortefoljeEnhet::enhetId).toSet()

                logger.info("Antall tilganger fra hentAdGroupsForUser ${adGrupperForNavIdent.size}")

                if (unikeEnhetTilgangerFraAxsys == unikeEnhetTilgangerFraADGrupper) {
                    logger.info("Enhettilganger er identiske mellom Axsys og AD-grupper.")
                } else {
                    logger.warn("Enhettilganger er ikke identiske mellom Axsys og AD-grupper.")
                }
            } catch (e: NavEnhetIdValideringException) {
                logger.warn(
                    "Kunne ikke hente enhettilganger fra AD-grupper. Årsak: validering feilet for en eller flere utlede NavEnhetId-er.",
                    e
                )
            }

            enhetTilgangerFraAxsys
        } else {
            hentEnheterFraAxsys(navIdent)
        }
    }

    fun hentEnheterFraAxsys(navIdent: NavIdent): List<PortefoljeEnhet> {
        return axsysClient.hentTilganger(navIdent)
            .map { axsysEnhet: AxsysEnhet? -> Mappers.tilPortefoljeEnhet(axsysEnhet) }
    }
    fun hentTilgangerFraEntraId(navIdent: NavIdent): List<AdGroupData> {
        return msGraphClient.hentAdGroupsForUser(azureAdMachineToMachineTokenClient.createMachineToMachineToken(environmentProperties.microsoftGraphScope),navIdent.toString())
    }

    fun hentEnhetTilgangerFraADGrupper(): Set<EnhetId> {
        return microsoftGraphClient.hentAdGrupper()
            .map { tilEnhetId(it.displayName) }
            .toSet()
    }

    companion object {
        private const val AD_GRUPPE_ENHET_PREFIKS = "0000-GA-ENHET_"
        private const val NAV_ENHET_ID_LENGDE = 4

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
