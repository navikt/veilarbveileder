package no.nav.veilarbveileder.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "app.env")
@ConstructorBinding
data class EnvironmentProperties (
    val naisStsDiscoveryUrl: String,
    val naisAadDiscoveryUrl: String,
    val naisAadClientId: String,
    val abacVeilarbUrl: String,
    val norg2Url: String,
    val unleashUrl: String,
    val poaoTilgangUrl: String,
    val poaoTilgangScope: String
)