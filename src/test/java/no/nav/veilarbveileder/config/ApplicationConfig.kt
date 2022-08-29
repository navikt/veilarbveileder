package no.nav.veilarbveileder.config

import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.NomClientImpl
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class ApplicationConfig {

    @Bean
    @Primary
    fun nomClient(): NomClient {
        return NomClientImpl("http://localhost:8080") { "" }
    }
}
