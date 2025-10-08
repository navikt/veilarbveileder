package no.nav.veilarbveileder.config

import io.getunleash.DefaultUnleash
import no.nav.common.auth.context.AuthContextHolder
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.msgraph.MsGraphClient
import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.NomClientImpl
import no.nav.common.client.norg2.Norg2Client
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient
import no.nav.poao_tilgang.client.PoaoTilgangClient
import no.nav.veilarbveileder.mock.PoaoTilgangClientMock
import no.nav.veilarbveileder.service.AuthService
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
    fun azureAdOnBehalfOfTokenClient(): AzureAdOnBehalfOfTokenClient {
        return Mockito.mock(AzureAdOnBehalfOfTokenClient::class.java)
    }

    @Bean
    fun msGraphClient(): MsGraphClient {
        return Mockito.mock(MsGraphClient::class.java)
    }

    @Bean
    fun authService(): AuthService {
        return Mockito.mock(AuthService::class.java)
    }

    @Bean
    fun norg2Client(): Norg2Client {
        return Mockito.mock(Norg2Client::class.java)
    }

    @Bean
    fun axsysClient(): AxsysClient {
        return Mockito.mock(AxsysClient::class.java)
    }

    @Bean
    fun defaultUnleash(): DefaultUnleash {
        return Mockito.mock(DefaultUnleash::class.java)
    }

    @Bean
    fun authContextHolder(): AuthContextHolder {
        return Mockito.mock(AuthContextHolder::class.java)
    }
}
