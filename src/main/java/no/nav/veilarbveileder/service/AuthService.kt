package no.nav.veilarbveileder.service;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.VeilarbPep;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.NavIdent;
import no.nav.poao_tilgang.client.Decision;
import no.nav.poao_tilgang.client.TilgangClient;
import no.nav.veilarbveileder.client.LdapClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthContextHolder authContextHolder;

    private final VeilarbPep veilarbPep;

    private final TilgangClient tilgangClient;

    private final LdapClient ldapClient;

    public static final String ROLLE_MODIA_ADMIN = "0000-GA-Modia_Admin";
    public static final List<String> ACCEPTLIST_AZURE_SYSTEM_USERS = List.of("veilarbfilter");

    public NavIdent getInnloggetVeilederIdent() {
        return authContextHolder.getNavIdent().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "NAV ident is missing"));
    }

    public String getInnloggetBrukerToken() {
        return authContextHolder.getIdTokenString().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing"));
    }

    public boolean erSystemBruker() {
        return authContextHolder.erSystemBruker();
    }

    public void sjekkTilgangTilOppfolging() {
        if (!veilarbPep.harTilgangTilOppfolging(getInnloggetBrukerToken())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ikke tilgang til oppfølging");
        }
    }

    public void sjekkTilgangTilModia() {
        Decision decisionPoaoTilgang = tilgangClient.harVeilederTilgangTilModia(getInnloggetVeilederIdent().get());
        boolean harTilgang = Decision.Type.PERMIT.equals(decisionPoaoTilgang.getType());
        if (!harTilgang) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ikke tilgang til modia");
        }
    }

    public void sjekkVeilederTilgangTilEnhet(EnhetId enhetId) {
        NavIdent ident = getInnloggetVeilederIdent();
        if (!harModiaAdminRolle(ident) && !veilarbPep.harVeilederTilgangTilEnhet(ident, enhetId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ikke tilgang til enhet");
        }
    }

    public boolean harModiaAdminRolle(NavIdent ident) {
        return ldapClient.veilederHarRolle(ident, ROLLE_MODIA_ADMIN);
    }


    public boolean erSystemBrukerFraAzureAd() {
        return erSystemBruker() && harAADRolleForSystemTilSystemTilgang();
    }

    private boolean harAADRolleForSystemTilSystemTilgang() {
        return authContextHolder.getIdTokenClaims()
                .flatMap(claims -> {
                    try {
                        return Optional.ofNullable(claims.getStringListClaim("roles"));
                    } catch (ParseException e) {
                        return Optional.empty();
                    }
                })
                .orElse(emptyList())
                .contains("access_as_application");
    }

    public boolean erGodkjentAzureAdSystembruker() {
        return ACCEPTLIST_AZURE_SYSTEM_USERS.contains(hentApplikasjonFraContex());
    }

    private String hentApplikasjonFraContex() {
        return authContextHolder.getIdTokenClaims()
                .flatMap(claims -> getStringClaimOrEmpty(claims, "azp_name")) //  "cluster:team:app"
                .map(claim -> claim.split(":"))
                .filter(claims -> claims.length == 3)
                .map(claims -> claims[2])
                .orElse(null);
    }

    private static Optional<String> getStringClaimOrEmpty(JWTClaimsSet claims, String claimName) {
        try {
            return ofNullable(claims.getStringClaim(claimName));
        } catch (Exception e) {
            return empty();
        }
    }
}
