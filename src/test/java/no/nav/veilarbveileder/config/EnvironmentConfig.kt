package no.nav.veilarbveileder.config

object EnvironmentConfig {
    fun setup() {
        System.setProperty("AAD_DISCOVERY_URL", "")
        System.setProperty("SECURITY_TOKEN_SERVICE_DISCOVERY_URL", "")
        System.setProperty("NORG2_URL", "")
        System.setProperty("UNLEASH_API_URL", "")
        System.setProperty("AZURE_APP_WELL_KNOWN_URL", "")
        System.setProperty("AZURE_APP_CLIENT_ID", "")
        System.setProperty("SRVVEILARBVEILEDER_USERNAME", "")
        System.setProperty("SRVVEILARBVEILEDER_PASSWORD", "")
        System.setProperty("POAO_TILGANG_SCOPE", "")
    }
}
