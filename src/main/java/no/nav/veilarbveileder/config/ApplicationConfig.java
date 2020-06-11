package no.nav.veilarbveileder.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.abac.Pep;
import no.nav.common.abac.VeilarbPep;
import no.nav.common.abac.audit.SpringAuditRequestInfoSupplier;
import no.nav.common.client.norg2.CachedNorg2Client;
import no.nav.common.client.norg2.Norg2Client;
import no.nav.common.client.norg2.NorgHttp2Client;
import no.nav.common.utils.Credentials;
import no.nav.veilarbveileder.client.LdapClient;
import no.nav.veilarbveileder.client.LdapClientImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static no.nav.veilarbveileder.utils.ServiceUserUtils.getServiceUserCredentials;

@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties({EnvironmentProperties.class})
public class ApplicationConfig {

    @Bean
    public Norg2Client norg2Client(EnvironmentProperties properties) {
        return new CachedNorg2Client(new NorgHttp2Client(properties.getNorg2Url()));
    }

    @Bean
    public Pep veilarbPep(EnvironmentProperties properties) {
        Credentials serviceUserCredentials = getServiceUserCredentials();
        return new VeilarbPep(
                properties.getAbacUrl(), serviceUserCredentials.username,
                serviceUserCredentials.password, new SpringAuditRequestInfoSupplier()
        );
    }

    @Bean
    public LdapClient ldapClient() {
        return new LdapClientImpl();
    }

}