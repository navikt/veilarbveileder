package no.nav.veilarbveileder.config

import no.nav.common.client.msgraph.MsGraphClient
import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.NomClientImpl
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient
import no.nav.poao_tilgang.client.PoaoTilgangClient
import no.nav.veilarbveileder.mock.PoaoTilgangClientMock
import org.mockito.Mockito
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@EnableConfigurationProperties(EnvironmentProperties::class)
@TestConfiguration
class ApplicationTestConfig {

    @Bean
    @Primary
    fun nomClient(): NomClient {
        return NomClientImpl("http://localhost:8080") { "" }
    }
    @Bean
    fun poaoTilgangClient(): PoaoTilgangClient {
        return PoaoTilgangClientMock()
    }

    @Bean
    fun azureAdMachineToMachineTokenClient(): AzureAdMachineToMachineTokenClient {
        return Mockito.mock(AzureAdMachineToMachineTokenClient::class.java)
    }

    @Bean
    fun msGraphClient(): MsGraphClient {
        return Mockito.mock(MsGraphClient::class.java)
    }
}
