package no.nav.veilarbveileder.config

object EnvironmentConfig {
    fun setup() {
        System.setProperty("OPENAM_DISCOVERY_URL", "")
        System.setProperty("VEILARBLOGIN_OPENAM_CLIENT_ID", "")
        System.setProperty("VEILARBLOGIN_OPENAM_REFRESH_URL", "")
        System.setProperty("AAD_DISCOVERY_URL", "")
        System.setProperty("VEILARBLOGIN_AAD_CLIENT_ID", "")
        System.setProperty("SECURITY_TOKEN_SERVICE_DISCOVERY_URL", "")
        System.setProperty("ABAC_PDP_ENDPOINT_URL", "")
        System.setProperty("NORG2_URL", "")
        System.setProperty("UNLEASH_API_URL", "")
        System.setProperty("AZURE_APP_WELL_KNOWN_URL", "")
        System.setProperty("AZURE_APP_CLIENT_ID", "")
        System.setProperty("SRVVEILARBVEILEDER_USERNAME", "")
        System.setProperty("SRVVEILARBVEILEDER_PASSWORD", "")
    }
}