package no.nav.veilarbveileder.service

import no.nav.common.types.identer.NavIdent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.norg2.Norg2Client
import no.nav.common.client.msgraph.MsGraphClient
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient
import io.getunleash.DefaultUnleash
import no.nav.veilarbveileder.client.MicrosoftGraphClient
import no.nav.veilarbveileder.config.EnvironmentProperties

@ExtendWith(MockitoExtension::class)
class EnhetServiceTest {

    @Mock
    private lateinit var norg2Client: Norg2Client

    @Mock
    private lateinit var axsysClient: AxsysClient

    @Mock
    private lateinit var microsoftGraphClient: MicrosoftGraphClient

    @Mock
    private lateinit var msGraphClient: MsGraphClient

    @Mock
    private lateinit var azureAdMachineToMachineTokenClient: AzureAdMachineToMachineTokenClient

    @Mock
    private lateinit var environmentProperties: EnvironmentProperties

    @Mock
    private lateinit var defaultUnleash: DefaultUnleash

    private lateinit var enhetService: EnhetService

    @BeforeEach
    fun setup() {
        enhetService = EnhetService(
            norg2Client,
            axsysClient,
            microsoftGraphClient,
            msGraphClient,
            azureAdMachineToMachineTokenClient,
            environmentProperties,
            defaultUnleash
        )
    }

    @Test
    fun `lagDifferanseSettForAnsatte - when sets are identical, returns empty differences`() {
        // Given
        val axsysIdenter = setOf(NavIdent("A123456"), NavIdent("B123456"))
        val adGruppeIdenter = setOf("A123456", "B123456")

        // When
        val (kunIAxsys, kunIADGrupper) = enhetService.lagDifferanseSettForAnsatte(axsysIdenter, adGruppeIdenter)

        // Then
        assertEquals(0, kunIAxsys.size)
        assertEquals(0, kunIADGrupper.size)
    }

    @Test
    fun `lagDifferanseSettForAnsatte - when items only in Axsys, returns correct differences`() {
        // Given
        val axsysIdenter = setOf(NavIdent("A123456"), NavIdent("B123456"), NavIdent("C123456"))
        val adGruppeIdenter = setOf("A123456", "B123456")

        // When
        val (kunIAxsys, kunIADGrupper) = enhetService.lagDifferanseSettForAnsatte(axsysIdenter, adGruppeIdenter)

        // Then
        assertEquals(1, kunIAxsys.size)
        assertEquals(NavIdent("C123456"), kunIAxsys.first())
        assertEquals(0, kunIADGrupper.size)
    }

    @Test
    fun `lagDifferanseSettForAnsatte - when items only in AD Groups, returns correct differences`() {
        // Given
        val axsysIdenter = setOf(NavIdent("A123456"), NavIdent("B123456"))
        val adGruppeIdenter = setOf("A123456", "B123456", "D123456")

        // When
        val (kunIAxsys, kunIADGrupper) = enhetService.lagDifferanseSettForAnsatte(axsysIdenter, adGruppeIdenter)

        // Then
        assertEquals(0, kunIAxsys.size)
        assertEquals(1, kunIADGrupper.size)
        assertEquals("D123456", kunIADGrupper.first())
    }

    @Test
    fun `lagDifferanseSettForAnsatte - when differences in both directions, returns all differences`() {
        // Given
        val axsysIdenter = setOf(NavIdent("A123456"), NavIdent("B123456"), NavIdent("C123456"))
        val adGruppeIdenter = setOf("A123456", "D123456", "E123456")

        // When
        val (kunIAxsys, kunIADGrupper) = enhetService.lagDifferanseSettForAnsatte(axsysIdenter, adGruppeIdenter)

        // Then
        assertEquals(2, kunIAxsys.size)
        assertEquals(2, kunIADGrupper.size)
        assertEquals(setOf(NavIdent("B123456"), NavIdent("C123456")), kunIAxsys)
        assertEquals(setOf("D123456", "E123456"), kunIADGrupper)
    }

    @Test
    fun `lagDifferanseSettForAnsatte - handles null values in Axsys set`() {
        // Given
        val axsysIdenter = setOf(NavIdent("A123456"), null, NavIdent("C123456"))
        val adGruppeIdenter = setOf("A123456", "C123456")

        // When
        val (kunIAxsys, kunIADGrupper) = enhetService.lagDifferanseSettForAnsatte(axsysIdenter, adGruppeIdenter)

        // Then
        assertEquals(1, kunIAxsys.size)
        assertEquals(null, kunIAxsys.first())
        assertEquals(0, kunIADGrupper.size)
    }
}