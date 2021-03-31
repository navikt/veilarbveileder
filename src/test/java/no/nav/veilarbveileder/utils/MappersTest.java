package no.nav.veilarbveileder.utils;

import no.nav.common.types.identer.EnhetId;
import no.nav.veilarbveileder.domain.PortefoljeEnhet;
import no.nav.veilarbveileder.domain.Veileder;
import no.nav.veilarbveileder.domain.VeilederInfo;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MappersTest {

    @Test
    public void skalMappeEnhetOgVeilederKorrekt() {
        EnhetId enhetId = EnhetId.of("0000");
        String enhetNavn = "Testheim";

        String forNavn = "Ola";
        String etterNavn = "Nordmann";
        String ident = "N123456";
        Veileder veileder = new Veileder().setNavn(forNavn + etterNavn)
                .setEtternavn(etterNavn).setFornavn(forNavn)
                .setIdent(ident);
        List<PortefoljeEnhet> enheter = List.of(new PortefoljeEnhet(enhetId,enhetNavn));

        VeilederInfo veilederInfo = Mappers.tilVeilederInfo(veileder, enheter);

        assertThat(veilederInfo.getIdent()).isEqualTo(ident);
        assertThat(veilederInfo.getNavn()).isEqualTo(forNavn + etterNavn);
        assertThat(veilederInfo.getFornavn()).isEqualTo(forNavn);
        assertThat(veilederInfo.getEtternavn()).isEqualTo(etterNavn);

        assertThat(veilederInfo.getEnheter()).isEqualTo(enheter);
    }
}
