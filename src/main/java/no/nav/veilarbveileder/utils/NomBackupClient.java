package no.nav.veilarbveileder.utils;

import no.nav.common.client.nom.NomClient;
import no.nav.common.client.nom.VeilederNavn;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.types.identer.NavIdent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * NOM har ikke støtte for å slå opp reelle veiledere eller Z-identer i test, så en alternativ klient må brukes
 */
@Service
public class NomBackupClient implements NomClient {

    @Override
    public VeilederNavn finnNavn(NavIdent navIdent) {
        return lagVeilederNavn(navIdent);
    }

    @Override
    public List<VeilederNavn> finnNavn(List<NavIdent> navIdenter) {
        return navIdenter.stream().map(this::lagVeilederNavn).collect(Collectors.toList());
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckResult.healthy();
    }

    private VeilederNavn lagVeilederNavn(NavIdent navIdent) {
        return new VeilederNavn()
                .setNavIdent(navIdent)
                .setFornavn(navIdent.toString())
                .setEtternavn(navIdent.toString())
                .setVisningsNavn(navIdent.toString());
    }

}
