package no.nav.veilarbveileder.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.env")
data class EnvironmentProperties (
    val naisAadDiscoveryUrl: String,
    val naisAadClientId: String,
    val norg2Url: String,
    val poaoTilgangUrl: String,
    val poaoTilgangScope: String,
    val nomApiUrl: String,
    val nomApiScope: String,
    val microsoftGraphUri: String,
    val microsoftGraphScope: String
)
