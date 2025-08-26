package no.nav.veilarbveileder.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.msgraph.MsGraphClient;
import no.nav.common.client.msgraph.UserData;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.NavIdent;
import no.nav.veilarbveileder.config.EnvironmentProperties;
import no.nav.veilarbveileder.domain.PortefoljeEnhet;
import no.nav.veilarbveileder.domain.VeiledereResponse;
import no.nav.veilarbveileder.service.AuthService;
import no.nav.veilarbveileder.service.EnhetService;
import no.nav.veilarbveileder.service.VeilederOgEnhetServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enhet")
@Slf4j
public class EnhetController {

    private final VeilederOgEnhetServiceV2 veilederOgEnhetService;

    private final EnhetService enhetService;

    private final AuthService authService;

    private final MsGraphClient msGraphClient;

    private final AzureAdMachineToMachineTokenClient azureAdMachineToMachineTokenClient;

    private final EnvironmentProperties environmentProperties;

    @Autowired
    public EnhetController(
            VeilederOgEnhetServiceV2 veilederOgEnhetService,
            EnhetService enhetService,
            AuthService authService,
            MsGraphClient msGraphClient,
            AzureAdMachineToMachineTokenClient azureAdMachineToMachineTokenClient,
            EnvironmentProperties environmentProperties
    ) {
        this.veilederOgEnhetService = veilederOgEnhetService;
        this.enhetService = enhetService;
        this.authService = authService;
        this.msGraphClient = msGraphClient;
        this.azureAdMachineToMachineTokenClient = azureAdMachineToMachineTokenClient;
        this.environmentProperties = environmentProperties;
    }

    @GetMapping("/{enhetId}/navn")
    public PortefoljeEnhet hentNavn(@PathVariable("enhetId") EnhetId enhetId) {
        return enhetService.hentEnhet(enhetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{enhetId}/veiledere")
    public VeiledereResponse hentRessurser(@PathVariable("enhetId") EnhetId enhetId) {
        authService.sjekkTilgangTilModia();
        authService.sjekkVeilederTilgangTilEnhet(enhetId);

        return veilederOgEnhetService.hentRessursListe(enhetId);
    }

    @GetMapping("/{enhetId}/identer")
    public List<NavIdent> hentIdenter(@PathVariable("enhetId") EnhetId enhetId) {
        if (authService.erSystemBrukerFraAzureAd() && authService.erGodkjentAzureAdSystembruker()) {
            return veilederOgEnhetService.hentIdentListe(enhetId);
        } else {
            authService.sjekkTilgangTilModia();
        }

        return veilederOgEnhetService.hentIdentListe(enhetId);
    }

    @GetMapping("/{groupId}/azure-user-data")
    public List<UserData> hentAnsatte(@PathVariable("groupId") UUID groupId) {
        authService.sjekkTilgangTilModia();

        return msGraphClient.hentUserDataForGroup(azureAdMachineToMachineTokenClient.createMachineToMachineToken(environmentProperties.getMicrosoftGraphScope()), groupId.toString());
    }

}
