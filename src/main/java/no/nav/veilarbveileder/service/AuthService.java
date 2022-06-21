package no.nav.veilarbveileder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.VeilarbPep;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.NavIdent;
import no.nav.poao_tilgang.client.Decision;
import no.nav.poao_tilgang.client.TilgangClient;
import no.nav.veilarbveileder.client.LdapClient;
import no.nav.veilarbveileder.utils.ModiaPep;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthContextHolder authContextHolder;

    private final VeilarbPep veilarbPep;

    private final TilgangClient tilgangClient;

    private final ModiaPep modiaPep;

    private final LdapClient ldapClient;

    private final UnleashClient unleashClient;

    public static final String ROLLE_MODIA_ADMIN = "0000-GA-Modia_Admin";

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
        boolean harTilgang = modiaPep.harVeilederTilgangTilModia(getInnloggetBrukerToken());
        if (unleashClient.isEnabled("veilarbveileder.poao-tilgang.sammenligne")) {
            try {
                Decision decisionPoaoTilgang = tilgangClient.harVeilederTilgangTilModia(getInnloggetVeilederIdent().get());
                boolean harTilgangPoaoTilgang = Decision.Type.PERMIT.equals(decisionPoaoTilgang.getType());
                if (harTilgang != harTilgangPoaoTilgang) {
                    log.warn("Forskjellig resultat fra poao-tilgang og abac-modia");
                }
            } catch (Exception e) {
                log.error("Kall til poao-tilgang feilet", e);
            }
        }
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

}
