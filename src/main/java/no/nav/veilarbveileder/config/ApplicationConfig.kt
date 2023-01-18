package no.nav.veilarbveileder.config

import com.github.benmanes.caffeine.cache.Caffeine
import lombok.extern.slf4j.Slf4j
import no.nav.common.abac.VeilarbPep
import no.nav.common.abac.VeilarbPepFactory
import no.nav.common.abac.audit.SpringAuditRequestInfoSupplier
import no.nav.common.auth.context.AuthContextHolder
import no.nav.common.auth.context.AuthContextHolderThreadLocal
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysClientImpl
import no.nav.common.client.axsys.AxsysEnhet
import no.nav.common.client.axsys.CachedAxsysClient
import no.nav.common.client.nom.CachedNomClient
import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.NomClientImpl
import no.nav.common.client.norg2.Norg2Client
import no.nav.common.client.norg2.NorgHttp2Client
import no.nav.common.featuretoggle.UnleashClient
import no.nav.common.featuretoggle.UnleashClientImpl
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient
import no.nav.common.types.identer.EnhetId
import no.nav.common.types.identer.NavIdent
import no.nav.common.utils.EnvironmentUtils
import no.nav.common.utils.UrlUtils
import no.nav.poao_tilgang.client.PoaoTilgangCachedClient
import no.nav.poao_tilgang.client.PoaoTilgangClient
import no.nav.poao_tilgang.client.PoaoTilgangHttpClient
import no.nav.veilarbveileder.client.LdapClient
import no.nav.veilarbveileder.client.LdapClientImpl
import no.nav.veilarbveileder.utils.DevNomClient
import no.nav.veilarbveileder.utils.ServiceUserUtils
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.concurrent.TimeUnit
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
    fun norg2Client(properties: EnvironmentProperties): Norg2Client {
        return (NorgHttp2Client(properties.norg2Url))
    }

    @Bean
    fun axsysClient(): AxsysClient {
        val url = UrlUtils.createServiceUrl("axsys", "org", false)
        val hentAnsatteCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(500)
            .build<EnhetId, List<NavIdent>>()
        val hentTilgangerCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build<NavIdent, List<AxsysEnhet>>()
        return CachedAxsysClient(AxsysClientImpl(url), hentTilgangerCache, hentAnsatteCache)
    }

    @Bean
    fun veilarbPep(properties: EnvironmentProperties): VeilarbPep {
        val serviceUserCredentials = ServiceUserUtils.getServiceUserCredentials()
        return VeilarbPepFactory.get(
            properties.abacVeilarbUrl, serviceUserCredentials.username,
            serviceUserCredentials.password, SpringAuditRequestInfoSupplier()
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
    fun ldapClient(): LdapClient {
        return LdapClientImpl()
    }

    @Bean
    fun authContextHolder(): AuthContextHolder {
        return AuthContextHolderThreadLocal.instance()
    }

    @Bean
    fun unleashClient(properties: EnvironmentProperties): UnleashClient {
        return UnleashClientImpl(properties.unleashUrl, APPLICATION_NAME)
    }

    @Bean
    fun nomClient(tokenClient: AzureAdMachineToMachineTokenClient): NomClient {
        if (EnvironmentUtils.isDevelopment().orElse(false)) {
            return DevNomClient()
        }
        val serviceTokenSupplier =
            Supplier { tokenClient.createMachineToMachineToken("api://prod-gcp.nom.nom-api/.default") }
        return CachedNomClient(NomClientImpl("https://nom-api.intern.nav.no", serviceTokenSupplier))
    }

    companion object {
        const val APPLICATION_NAME = "veilarbveileder"
    }
}