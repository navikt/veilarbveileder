package no.nav.veilarbveileder.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class VeilederInfo {
    public VeilederInfo(Veileder veileder, List<PortefoljeEnhet> enheter) {
        this.ident = veileder.getIdent();
        this.navn = veileder.getNavn();
        this.fornavn = veileder.getFornavn();
        this.etternavn = veileder.getEtternavn();
        this.enheter = enheter;
    }

    String ident;
    String navn;
    String fornavn;
    String etternavn;
    List<PortefoljeEnhet> enheter;
}
