package no.nav.veilarbveileder.utils;

import no.nav.common.abac.AbacClient;
import no.nav.common.abac.Pep;
import no.nav.common.abac.domain.request.ActionId;
import no.nav.common.types.identer.EksternBrukerId;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.NavIdent;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Midlertidig Pep implementasjon for å bruke "modia"-domenet i ABAC som ikke er tilgjengelig på samme URL som "veilarb"-domenet når man tar i bruk ABAC på NAIS.
 * TODO: En litt bedre løsning er at dette løses i common og at {@link #harVeilederTilgangTilModia} flyttes til egen pep
 */
public class ModiaPep implements Pep {

    private final Pep pep;

    public ModiaPep(Pep pep) {
        this.pep = pep;
    }

    @Override
    public boolean harVeilederTilgangTilEnhet(NavIdent navIdent, EnhetId enhetId) {
        throw new NotImplementedException("Not available for ABAC modia");
    }

    @Override
    public boolean harTilgangTilEnhet(String s, EnhetId enhetId) {
        throw new NotImplementedException("Not available for ABAC modia");
    }

    @Override
    public boolean harTilgangTilEnhetMedSperre(String s, EnhetId enhetId) {
        throw new NotImplementedException("Not available for ABAC modia");
    }

    @Override
    public boolean harVeilederTilgangTilPerson(NavIdent navIdent, ActionId actionId, EksternBrukerId eksternBrukerId) {
        throw new NotImplementedException("Not available for ABAC modia");
    }

    @Override
    public boolean harTilgangTilPerson(String s, ActionId actionId, EksternBrukerId eksternBrukerId) {
        throw new NotImplementedException("Not available for ABAC modia");
    }

    @Override
    public boolean harTilgangTilOppfolging(String s) {
        throw new NotImplementedException("Not available for ABAC modia");
    }

    @Override
    public boolean harVeilederTilgangTilModia(String s) {
        return pep.harVeilederTilgangTilModia(s);
    }

    @Override
    public boolean harVeilederTilgangTilKode6(NavIdent navIdent) {
        throw new NotImplementedException("Not available for ABAC modia");
    }

    @Override
    public boolean harVeilederTilgangTilKode7(NavIdent navIdent) {
        throw new NotImplementedException("Not available for ABAC modia");
    }

    @Override
    public boolean harVeilederTilgangTilEgenAnsatt(NavIdent navIdent) {
        throw new NotImplementedException("Not available for ABAC modia");
    }

    @Override
    public AbacClient getAbacClient() {
        return pep.getAbacClient();
    }
}
