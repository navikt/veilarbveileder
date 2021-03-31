package no.nav.veilarbveileder.utils;

import no.nav.veilarbveileder.domain.VeiledereResponse;
import no.nav.virksomhet.organisering.enhetogressurs.v1.Enhet;
import no.nav.virksomhet.organisering.enhetogressurs.v1.Ressurs;
import no.nav.virksomhet.tjenester.enhet.meldinger.v1.WSHentRessursListeResponse;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MappersTest {


    private Enhet createEnhet(String enhetId, String navn) {
        no.nav.virksomhet.organisering.enhetogressurs.v1.Enhet enhet = mock(no.nav.virksomhet.organisering.enhetogressurs.v1.Enhet.class);
        when(enhet.getEnhetId()).thenReturn(enhetId);
        when(enhet.getNavn()).thenReturn(navn);
        return enhet;
    }

    private Ressurs createRessurs() {
        Ressurs ressurs = new Ressurs();
        ressurs.setRessursId("ressurs id");
        ressurs.setNavn("fornavn etternavn");
        ressurs.setEtternavn("etternavn");
        ressurs.setFornavn("fornavn");
        return ressurs;
    }

    private Ressurs createAnotherRessurs() {
        Ressurs ressurs = new Ressurs();
        ressurs.setRessursId("another ressurs id");
        ressurs.setNavn("another fornavn etternavn");
        ressurs.setEtternavn("another etternavn");
        ressurs.setFornavn("another fornavn");
        return ressurs;
    }
}
