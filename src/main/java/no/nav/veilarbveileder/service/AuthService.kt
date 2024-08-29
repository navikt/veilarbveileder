package no.nav.veilarbveileder.service

import com.nimbusds.jwt.JWTClaimsSet
import lombok.extern.slf4j.Slf4j
import no.nav.common.auth.context.AuthContextHolder
import no.nav.common.types.identer.EnhetId
import no.nav.common.types.identer.NavIdent
import no.nav.poao_tilgang.client.NavAnsattTilgangTilModiaAdminPolicyInput
import no.nav.poao_tilgang.client.NavAnsattTilgangTilModiaPolicyInput
import no.nav.poao_tilgang.client.NavAnsattTilgangTilNavEnhetPolicyInput
import no.nav.poao_tilgang.client.PoaoTilgangClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.text.ParseException
import java.util.*

@Service
@Slf4j
class AuthService(
    private val authContextHolder: AuthContextHolder,
    private val poaoTilgangClient: PoaoTilgangClient
) {
    val innloggetVeilederIdent: NavIdent
        get() = authContextHolder.navIdent.orElseThrow {
            ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "NAV ident is missing"
            )
        }

    fun erSystemBruker(): Boolean {
        return authContextHolder.erSystemBruker()
    }
    fun hentInnloggetVeilederUUID(): UUID =
        authContextHolder
            .idTokenClaims.flatMap { authContextHolder.getStringClaim(it, "oid") }
            .map { UUID.fromString(it) }
            .orElseThrow { ResponseStatusException(HttpStatus.FORBIDDEN, "Fant ikke oid for innlogget veileder") }

    fun sjekkTilgangTilModia() {
        val tilgangResult = poaoTilgangClient.evaluatePolicy(NavAnsattTilgangTilModiaPolicyInput(
            hentInnloggetVeilederUUID())
        ).getOrThrow()
        if (tilgangResult.isDeny) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ikke tilgang til modia")
        }
    }

    fun sjekkVeilederTilgangTilEnhet(enhetId: EnhetId?) {
        val tilgangResult = poaoTilgangClient.evaluatePolicy(
            NavAnsattTilgangTilNavEnhetPolicyInput(hentInnloggetVeilederUUID(), enhetId.toString())
        ).getOrThrow()
        if (tilgangResult.isDeny) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ikke tilgang til enhet")
        }
    }

    fun harModiaAdminRolle(): Boolean {
        val tilgangResult = poaoTilgangClient.evaluatePolicy(
            NavAnsattTilgangTilModiaAdminPolicyInput(hentInnloggetVeilederUUID())
        ).getOrThrow()
        return tilgangResult.isPermit
    }

    fun erSystemBrukerFraAzureAd(): Boolean {
        return erSystemBruker() && harAADRolleForSystemTilSystemTilgang()
    }

    private fun harAADRolleForSystemTilSystemTilgang(): Boolean {
        return authContextHolder.idTokenClaims
            .flatMap { claims: JWTClaimsSet ->
                try {
                    return@flatMap Optional.ofNullable<List<String>>(claims.getStringListClaim("roles"))
                } catch (e: ParseException) {
                    return@flatMap Optional.empty<List<String>>()
                }
            }
            .orElse(emptyList())
            .contains("access_as_application")
    }

    fun erGodkjentAzureAdSystembruker(): Boolean {
        return ACCEPTLIST_AZURE_SYSTEM_USERS.contains(hentApplikasjonFraContex())
    }

    private fun hentApplikasjonFraContex(): String? {
        return authContextHolder.idTokenClaims
            .flatMap { claims: JWTClaimsSet -> getStringClaimOrEmpty(claims, "azp_name") } //  "cluster:team:app"
            .map { claim: String ->
                claim.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            }
            .filter { claims: Array<String> -> claims.size == 3 }
            .map { claims: Array<String> -> claims[2] }
            .orElse(null)
    }

    companion object {
        const val ROLLE_MODIA_ADMIN = "0000-GA-Modia_Admin"
        val ACCEPTLIST_AZURE_SYSTEM_USERS = listOf("veilarbfilter", "veilarbportefolje","veilarbvedtaksstotte")
        private fun getStringClaimOrEmpty(claims: JWTClaimsSet, claimName: String): Optional<String> {
            return try {
                Optional.ofNullable(claims.getStringClaim(claimName))
            } catch (e: Exception) {
                Optional.empty()
            }
        }
    }
}
