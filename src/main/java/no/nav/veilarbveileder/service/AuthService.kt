package no.nav.veilarbveileder.service

import com.nimbusds.jwt.JWTClaimsSet
import lombok.extern.slf4j.Slf4j
import no.nav.common.abac.VeilarbPep
import no.nav.common.auth.context.AuthContextHolder
import no.nav.common.types.identer.EnhetId
import no.nav.common.types.identer.NavIdent
import no.nav.poao_tilgang.client.*
import no.nav.veilarbveileder.client.LdapClient
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.text.ParseException
import java.util.*

@Service
@Slf4j
class AuthService(
    private val authContextHolder: AuthContextHolder,
    private val veilarbPep: VeilarbPep,
    private val poaoTilgangClient: PoaoTilgangClient,
    private val unleashService: UnleashService,
    private val ldapClient: LdapClient) {
    val innloggetVeilederIdent: NavIdent
        get() = authContextHolder.navIdent.orElseThrow {
            ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "NAV ident is missing"
            )
        }

    val innloggetBrukerToken: String
        get() = authContextHolder.idTokenString.orElseThrow {
            ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Token is missing"
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

    fun sjekkTilgangTilOppfolging() {
        if (!veilarbPep.harTilgangTilOppfolging(innloggetBrukerToken)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ikke tilgang til oppfølging")
        }
    }
    fun sjekkTilgangTilModia() {
        if (unleashService.isPoaoTilgangEnabled) {
            val tilgangResult = poaoTilgangClient.evaluatePolicy(NavAnsattTilgangTilModiaPolicyInput(
                hentInnloggetVeilederUUID())
            ).getOrThrow()
            if (tilgangResult.isDeny) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ikke tilgang til modia")
            }
        } else {
            sjekkTilgangTilOppfolging()
        }
    }

    fun sjekkVeilederTilgangTilEnhet(enhetId: EnhetId?) {
        val ident = innloggetVeilederIdent

        if (unleashService.isPoaoTilgangEnabled) {
           if (!harModiaAdminRolle(ident)) {
               val tilgangResult = poaoTilgangClient.evaluatePolicy(
                   NavAnsattTilgangTilNavEnhetPolicyInput(hentInnloggetVeilederUUID(), enhetId.toString())
               ).getOrThrow()
               if (tilgangResult.isDeny) {
                   throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ikke tilgang til enhet")
               }
           }
        } else {
            if (!harModiaAdminRolle(ident) && !veilarbPep.harVeilederTilgangTilEnhet(ident, enhetId)) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ikke tilgang til enhet")
            }
        }
    }

    fun harModiaAdminRolle(ident: NavIdent?): Boolean {
        return ldapClient.veilederHarRolle(ident, ROLLE_MODIA_ADMIN)
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
        val ACCEPTLIST_AZURE_SYSTEM_USERS = java.util.List.of("veilarbfilter")
        private fun getStringClaimOrEmpty(claims: JWTClaimsSet, claimName: String): Optional<String> {
            return try {
                Optional.ofNullable(claims.getStringClaim(claimName))
            } catch (e: Exception) {
                Optional.empty()
            }
        }
    }
}