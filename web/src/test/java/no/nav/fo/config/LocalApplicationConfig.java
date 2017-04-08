package no.nav.fo.config;

import no.nav.fo.service.PepClientInterface;
import no.nav.fo.service.PepClientMock;
import no.nav.sbl.dialogarena.common.abac.pep.context.AbacContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        VirksomhetEnhetEndpointConfigMock.class,
        ServiceConfig.class,
        CacheConfig.class,
        AbacContext.class
})
public class LocalApplicationConfig {

    @Bean
    public PepClientInterface pepClient() { return new PepClientMock(); }

}
