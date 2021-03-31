package no.nav.veilarbveileder.utils;

import no.nav.common.client.axsys.AxsysEnhet;
import no.nav.common.client.norg2.Enhet;
import no.nav.common.types.identer.EnhetId;
import no.nav.veilarbveileder.domain.PortefoljeEnhet;

public class Mappers {

    public static PortefoljeEnhet tilPortefoljeEnhet(Enhet enhet) {
        if(enhet == null){
            return null;
        }
        return new PortefoljeEnhet(EnhetId.of(enhet.getEnhetNr()), enhet.getNavn());
    }

    public static PortefoljeEnhet tilPortefoljeEnhet(AxsysEnhet axsysEnhet){
        if(axsysEnhet == null){
            return null;
        }
        return new PortefoljeEnhet(axsysEnhet.getEnhetId(), axsysEnhet.getNavn());
    }
}
