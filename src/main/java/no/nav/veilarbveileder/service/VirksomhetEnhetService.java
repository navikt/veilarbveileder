package no.nav.veilarbveileder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.axsys.AxsysEnhet;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.NavIdent;
import no.nav.common.client.axsys.AxsysClient;
import no.nav.veilarbveileder.domain.PortefoljeEnhet;
import no.nav.veilarbveileder.domain.Veileder;
import no.nav.veilarbveileder.domain.VeilederInfo;
import no.nav.veilarbveileder.domain.VeiledereResponse;
import no.nav.veilarbveileder.utils.Mappers;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.veilarbveileder.service.AuthService.ROLLE_MODIA_ADMIN;
import static no.nav.veilarbveileder.utils.Mappers.tilPortefoljeEnhet;

@Slf4j
@Service
@RequiredArgsConstructor
public class VirksomhetEnhetService {

    private final AuthService authService;

    private final Norg2Client norg2Client;

    private final AxsysClient axsysClient;

    private final AzureService azureService;

    public List<PortefoljeEnhet> hentEnhetListe(NavIdent navIdent) {
        final boolean harModiaAdminRolle = authService.harModiaAdminRolle(navIdent);

        if (harModiaAdminRolle) {
            log.info("Rollen {} ble brukt for ident: {}", ROLLE_MODIA_ADMIN, navIdent);
            return alleEnheter();
        }
        List<AxsysEnhet> axsysEnhets = axsysClient.hentTilganger(navIdent);
        return axsysEnhets.stream().map(Mappers::tilPortefoljeEnhet).collect(Collectors.toList());
    }

    public Veileder hentVeilederData(NavIdent navIdent) {
        return azureService.getVeilederInfo(navIdent);
    }

    public VeilederInfo hentVeilederInfo(NavIdent navIdent) {
        final boolean harModiaAdminRolle = authService.harModiaAdminRolle(navIdent);
        Veileder veileder = hentVeilederData(navIdent);
        List<PortefoljeEnhet> portefoljeEnheter = hentEnhetListe(navIdent);

        VeilederInfo veilederInfo = new VeilederInfo(veileder, portefoljeEnheter);

        if (harModiaAdminRolle) {
            log.info("Rollen {} ble brukt for ident: {}", ROLLE_MODIA_ADMIN, navIdent);
            veilederInfo.setEnheter(alleEnheter());
        }

        return veilederInfo;
    }

    public VeiledereResponse hentRessursListe(EnhetId enhetId) {
        PortefoljeEnhet enhet = tilPortefoljeEnhet(norg2Client.hentEnhet(enhetId.get()));
        List<NavIdent> navIdenter = axsysClient.hentAnsatte(enhetId);
        List<Veileder> veiledere = azureService.getVeilederInfo(navIdenter);

        return new VeiledereResponse(enhet, veiledere);
    }

    public List<String> hentIdentListe(EnhetId enhetId) {
        List<NavIdent> veilederIder = axsysClient.hentAnsatte(enhetId);
        return veilederIder.stream().map(NavIdent::get).collect(Collectors.toList());
    }

    private List<PortefoljeEnhet> alleEnheter() {
        return norg2Client.alleAktiveEnheter()
                .stream()
                .map(Mappers::tilPortefoljeEnhet)
                .collect(Collectors.toList());
    }

}
