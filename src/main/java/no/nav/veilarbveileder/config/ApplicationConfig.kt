package no.nav.veilarbveileder.config

import io.getunleash.DefaultUnleash
import io.getunleash.util.UnleashConfig
import lombok.extern.slf4j.Slf4j
import no.nav.common.auth.context.AuthContextHolder
import no.nav.common.auth.context.AuthContextHolderThreadLocal
import no.nav.common.client.msgraph.CachedMsGraphClient
import no.nav.common.client.msgraph.MsGraphClient
import no.nav.common.client.msgraph.MsGraphHttpClient
import no.nav.common.client.nom.CachedNomClient
import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.NomClientImpl
import no.nav.common.client.norg2.Norg2Client
import no.nav.common.client.norg2.NorgHttp2Client
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient
import no.nav.common.utils.EnvironmentUtils
import no.nav.common.utils.EnvironmentUtils.isProduction
import no.nav.poao_tilgang.client.PoaoTilgangCachedClient
import no.nav.poao_tilgang.client.PoaoTilgangClient
import no.nav.poao_tilgang.client.PoaoTilgangHttpClient
import no.nav.veilarbveileder.utils.DevNomClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.function.Supplier


@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties(
    EnvironmentProperties::class
)
class ApplicationConfig {
    @Bean
    fun azureAdMachineToMachineTokenClient(): AzureAdMachineToMachineTokenClient {
        return AzureAdTokenClientBuilder.builder()
            .withNaisDefaults()
            .buildMachineToMachineTokenClient()
    }

    @Bean
    fun azureAdOnBehalfOfTokenClient(): AzureAdOnBehalfOfTokenClient {
        return AzureAdTokenClientBuilder.builder()
            .withNaisDefaults()
            .buildOnBehalfOfTokenClient()
    }

    @Bean
    fun norg2Client(properties: EnvironmentProperties): Norg2Client {
        return NorgHttp2Client(properties.norg2Url)
    }

    @Bean
    fun msGraphClient(
        properties: EnvironmentProperties
    ): MsGraphClient {
        return CachedMsGraphClient(
            MsGraphHttpClient(
                properties.microsoftGraphUri
            )
        )
    }

    @Bean
    fun poaoTilgangClient(
        properties: EnvironmentProperties,
        tokenClient: AzureAdMachineToMachineTokenClient
    ): PoaoTilgangClient {
        return PoaoTilgangCachedClient(
            PoaoTilgangHttpClient(
                properties.poaoTilgangUrl,
                { tokenClient.createMachineToMachineToken(properties.poaoTilgangScope) })
        )
    }

    @Bean
    fun authContextHolder(): AuthContextHolder {
        return AuthContextHolderThreadLocal.instance()
    }

    @Bean
    fun nomClient(
        tokenClient: AzureAdMachineToMachineTokenClient,
        environmentProperties: EnvironmentProperties
    ): NomClient {
        if (EnvironmentUtils.isDevelopment().orElse(false)) {
            return DevNomClient()
        }
        val serviceTokenSupplier =
            Supplier { tokenClient.createMachineToMachineToken(environmentProperties.nomApiScope) }
        return CachedNomClient(NomClientImpl(environmentProperties.nomApiUrl, serviceTokenSupplier))
    }

    @Bean
    open fun unleashClient(
        @Value("\${nais.env.unleash.url}") unleashUrl: String,
        @Value("\${nais.env.unleash.apiToken}") unleashApiToken: String,
        @Value("\${nais.env.podName}") podName: String
    ): DefaultUnleash = DefaultUnleash(
        UnleashConfig.builder()
            .appName(APPLICATION_NAME)
            .instanceId(podName)
            .unleashAPI("$unleashUrl/api")
            .apiKey(unleashApiToken)
            .environment(if (isProduction().orElse(false)) "production" else "development")
            .synchronousFetchOnInitialisation(true)
            .build()
    )

    companion object {
        const val APPLICATION_NAME = "veilarbveileder"
    }
}
