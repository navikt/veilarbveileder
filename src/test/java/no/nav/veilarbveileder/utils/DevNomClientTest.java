package no.nav.veilarbveileder.utils;

import no.nav.common.client.nom.VeilederNavn;
import no.nav.common.types.identer.NavIdent;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DevNomClientTest {

    @Test
    public void skal_lage_navn_for_ident() {
        NomBackupClient nomBackupClient = new NomBackupClient();

        NavIdent ident = NavIdent.of("Z123456");

        VeilederNavn veilederNavn = nomBackupClient.finnNavn(ident);

        assertEquals(ident, veilederNavn.getNavIdent());
        assertEquals("Z123456", veilederNavn.getFornavn());
        assertEquals("Z123456", veilederNavn.getEtternavn());
        assertEquals("Z123456", veilederNavn.getVisningsNavn());
    }

    @Test
    public void skal_lage_navn_for_identer() {
        NomBackupClient nomBackupClient = new NomBackupClient();

        VeilederNavn veileder1 = new VeilederNavn()
                .setNavIdent(NavIdent.of("Z444444"))
                .setFornavn("Z444444")
                .setEtternavn("Z444444")
                .setVisningsNavn("Z444444");

        VeilederNavn veileder2 = new VeilederNavn()
                .setNavIdent(NavIdent.of("Z777777"))
                .setFornavn("Z777777")
                .setEtternavn("Z777777")
                .setVisningsNavn("Z777777");

        List<VeilederNavn> veilederNavn = nomBackupClient.finnNavn(List.of(veileder1.getNavIdent(), veileder2.getNavIdent()));

        assertEquals(2, veilederNavn.size());
        assertEquals(veileder1, veilederNavn.get(0));
        assertEquals(veileder2, veilederNavn.get(1));
    }


}
