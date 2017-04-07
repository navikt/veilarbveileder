package no.nav.fo.config;

import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.virksomhet.tjenester.enhet.v1.Enhet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VirksomhetEnhetEndpointConfig {

    @Bean
    public Enhet virksomhetEnhet() {
        return new CXFClient<>(Enhet.class)
                .address(System.getProperty("norg.virksomhet_enhet.url"))
                .configureStsForOnBehalfOfWithJWT()
                .build();
    }

    @Bean
    public Pingable virksomhetEnhetPing() {
        Enhet virksomhetEnhet = new CXFClient<>(Enhet.class)
                .address(System.getProperty("norg.virksomhet_enhet.url"))
                .withOutInterceptor(new SystemSAMLOutInterceptor())
                .build();

        return () -> {
            try {
                virksomhetEnhet.ping();
                return Pingable.Ping.lyktes("VirksomhetEnhet");
            } catch (Exception e) {
                return Pingable.Ping.feilet("VirksomhetEnhet", e);
            }
        };
    }
}
