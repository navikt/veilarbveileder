package no.nav.veilarbveileder.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.env")
public class EnvironmentProperties {

    private String openAmDiscoveryUrl;

    private String veilarbloginOpenAmClientId;

    private String openAmRefreshUrl;

    private String aadDiscoveryUrl;

    private String veilarbloginAadClientId;

    private String naisStsDiscoveryUrl;

    private String abacUrl;

    private String norg2Url;

}
