package no.nav.veilarbveileder.controller;

import no.nav.common.types.identer.NavIdent;
import no.nav.veilarbveileder.controller.dto.ListVeiledereRequest;
import no.nav.veilarbveileder.domain.IdentOgEnhetliste;
import no.nav.veilarbveileder.domain.PortefoljeEnhet;
import no.nav.veilarbveileder.domain.Veileder;
import no.nav.veilarbveileder.domain.VeilederInfo;
import no.nav.veilarbveileder.service.AuthService;
import no.nav.veilarbveileder.service.VeilederOgEnhetServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.ws.rs.Produces;
import java.util.List;

@RestController
@RequestMapping("/api/veileder")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public class VeilederController {

    private final VeilederOgEnhetServiceV2 veilederOgEnhetService;

    private final AuthService authService;

    @Autowired
    public VeilederController(VeilederOgEnhetServiceV2 veilederOgEnhetService, AuthService authService) {
        this.veilederOgEnhetService = veilederOgEnhetService;
        this.authService = authService;
    }

    @GetMapping("/enheter")
    public IdentOgEnhetliste hentEnheter() {
        authService.sjekkTilgangTilModia();
        NavIdent navIdent = authService.getInnloggetVeilederIdent();
        List<PortefoljeEnhet> response = veilederOgEnhetService.hentEnhetListe(navIdent);
        return new IdentOgEnhetliste(navIdent, response);
    }

    @GetMapping("/enheter/{veilederIdent}")
    public IdentOgEnhetliste hentEnheter(@PathVariable("veilederIdent") NavIdent veilederIdent) {
        if (!authService.erSystemBrukerFraAzureAd()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ikke tilgang til oppfølging");
        }

        List<PortefoljeEnhet> response = veilederOgEnhetService.hentEnhetListe(veilederIdent);
        return new IdentOgEnhetliste(veilederIdent, response);
    }

    @GetMapping("/me")
    public Veileder hentVeilederData() {
        authService.sjekkTilgangTilModia();
        return veilederOgEnhetService.hentVeilederData(authService.getInnloggetVeilederIdent());
    }

    @PostMapping("/list")
    public List<Veileder> hentVeilederForIdent(@RequestBody ListVeiledereRequest request) {
        authService.sjekkTilgangTilModia();
        return veilederOgEnhetService.hentVeiledereData(request.getIdenter());
    }

    @GetMapping("/v2/me")
    public VeilederInfo hentVeilederInfo() {
        authService.sjekkTilgangTilModia();
        return veilederOgEnhetService.hentVeilederInfo(authService.getInnloggetVeilederIdent());
    }

    @GetMapping("/{ident}")
    public Veileder hentVeilederForIdent(@PathVariable("ident") NavIdent ident) {
        authService.sjekkTilgangTilModia();
        return veilederOgEnhetService.hentVeilederData(ident);
    }

    @PostMapping("/hent-navn")
    public String hentVeilederNavn(@RequestBody NavIdent ident) {
        if(authService.erSystemBrukerFraAzureAd() && authService.erGodkjentAzureAdSystembruker()){
            return veilederOgEnhetService.hentVeilederData(ident).getNavn();
        }
        else {
            authService.sjekkTilgangTilModia();
            return veilederOgEnhetService.hentVeilederData(ident).getNavn();
        }
    }

}
