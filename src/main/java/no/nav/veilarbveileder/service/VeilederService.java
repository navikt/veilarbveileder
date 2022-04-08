package no.nav.veilarbveileder.service;

import lombok.RequiredArgsConstructor;
import no.nav.common.client.nom.NomClient;
import no.nav.common.client.nom.VeilederNavn;
import no.nav.common.types.identer.NavIdent;
import no.nav.veilarbveileder.domain.Veileder;
import no.nav.veilarbveileder.utils.NomBackupClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class VeilederService {
    private final NomClient nomClient;
    private final NomBackupClient nomBackUpClient;

    public Veileder hentVeileder(NavIdent navIdent) {
        try {
            return tilVeileder(nomClient.finnNavn(navIdent));
        } catch (Exception e) {
            return tilVeileder(nomBackUpClient.finnNavn(navIdent));
        }
    }

    public List<Veileder> hentVeiledere(List<NavIdent> navIdenter) {
        try {
            return nomClient.finnNavn(navIdenter).stream().map(VeilederService::tilVeileder).collect(Collectors.toList());
        } catch (Exception e) {
            return nomBackUpClient.finnNavn(navIdenter).stream().map(VeilederService::tilVeileder).collect(Collectors.toList());
        }
    }

    private static Veileder tilVeileder(VeilederNavn veilederNavn) {
        String fornavn = veilederNavn.getFornavn();
        String etternavn = veilederNavn.getEtternavn();
        String visningsNavn = veilederNavn.getVisningsNavn();

        return new Veileder()
                .setIdent(veilederNavn.getNavIdent().get())
                .setFornavn(fornavn)
                .setEtternavn(etternavn)
                .setNavn(visningsNavn);
    }

}
