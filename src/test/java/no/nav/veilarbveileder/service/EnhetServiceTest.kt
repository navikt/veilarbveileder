package no.nav.veilarbveileder.service

import no.nav.common.types.identer.NavIdent
import org.junit.jupiter.api.Assertions.*
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
import no.nav.common.types.identer.EnhetId
import no.nav.veilarbveileder.client.MicrosoftGraphClient
import no.nav.veilarbveileder.config.EnvironmentProperties
import no.nav.veilarbveileder.utils.BRUK_VEILEDERE_PAA_ENHET_FRA_AD
import no.nav.veilarbveileder.utils.HENT_ENHETER_FRA_AD_OG_LOGG_DIFF

import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq

import no.nav.common.client.norg2.Enhet
import no.nav.common.client.axsys.AxsysEnhet
import no.nav.common.client.msgraph.UserData
import no.nav.veilarbveileder.client.AdGruppe
import org.mockito.Mockito.*


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
    fun `hentEnhet returns optional with enhet when norg2 returns unit`() {
        val enhetId = EnhetId.of("1234")
        val enhet = mock(Enhet::class.java)
        `when`(enhet.enhetNr).thenReturn("1234")
        `when`(enhet.navn).thenReturn("NAV Test")

        `when`(norg2Client.hentEnhet("1234")).thenReturn(enhet)

        val result = enhetService.hentEnhet(enhetId)

        assertTrue(result.isPresent)
        assertEquals(EnhetId.of("1234"), result.get().enhetId)
        assertEquals("NAV Test", result.get().navn)
    }

    @Test
    fun `hentEnhet returns empty optional when norg2 throws exception`() {
        val enhetId = EnhetId.of("1234")

        `when`(norg2Client.hentEnhet("1234")).thenThrow(RuntimeException("Not found"))

        val result = enhetService.hentEnhet(enhetId)

        assertTrue(result.isEmpty)
    }

    @Test
    fun `alleEnheter returns mapped portefoljeEnheter`() {
        // Mock the Enhet objects that would come from norg2Client
        val enhet1 = mock(Enhet::class.java)
        val enhet2 = mock(Enhet::class.java)

        // Set up properties on mock PortefoljeEnhet objects
        `when`(enhet1.enhetNr).thenReturn("1234")
        `when`(enhet1.navn).thenReturn("Nav-kontor 1")
        `when`(enhet2.enhetNr).thenReturn("5678")
        `when`(enhet2.navn).thenReturn("Nav-kontor 2")

        // Mock norg2Client.alleAktiveEnheter() to return our mock Enhet objects
        `when`(norg2Client.alleAktiveEnheter()).thenReturn(listOf(enhet1, enhet2))


        // Call the method under test
        val result = enhetService.alleEnheter()

        // Assert on the returned PortefoljeEnhet objects
        assertEquals(2, result.size)
        assertEquals(EnhetId.of("1234"), result[0]?.enhetId)
        assertEquals(EnhetId.of("5678"), result[1]?.enhetId)
    }

    @Test
    fun `veilederePaEnhet gets users from MS Graph when feature flag is enabled`() {
        val enhetId = EnhetId.of("1234")
        val userData1 = UserData().apply { onPremisesSamAccountName = "A123456" }
        val userData2 = UserData().apply { onPremisesSamAccountName = "B789012" }
        val token = "token123"

        `when`(defaultUnleash.isEnabled(BRUK_VEILEDERE_PAA_ENHET_FRA_AD)).thenReturn(true)
        `when`(azureAdMachineToMachineTokenClient.createMachineToMachineToken(any())).thenReturn(token)
        `when`(msGraphClient.hentUserDataForGroup(eq(token), eq(enhetId))).thenReturn(listOf(userData1, userData2))

        val result = enhetService.veilederePaEnhet(enhetId)

        assertEquals(2, result?.size)
        assertEquals("A123456", result?.get(0)?.get())
        assertEquals("B789012", result?.get(1)?.get())
    }

    @Test
    fun `veilederePaEnhet gets users from Axsys when feature flag is disabled`() {
        val enhetId = EnhetId.of("1234")
        val navIdents = listOf(NavIdent.of("A123456"), NavIdent.of("B789012"))

        `when`(defaultUnleash.isEnabled(BRUK_VEILEDERE_PAA_ENHET_FRA_AD)).thenReturn(false)
        `when`(axsysClient.hentAnsatte(enhetId)).thenReturn(navIdents)

        val result = enhetService.veilederePaEnhet(enhetId)

        assertEquals(navIdents, result)
    }

    @Test
    fun `hentTilganger compares results when feature flag is enabled`() {
        val navIdent = NavIdent.of("A123456")
        val axsysEnhet = mock(AxsysEnhet::class.java)
        `when`(axsysEnhet.enhetId).thenReturn(EnhetId.of("1234"))
        `when`(axsysEnhet.navn).thenReturn("Group 1234")

        val adGroup = AdGruppe("0000-GA-ENHET_1234", "Nav-kontoret")

        `when`(defaultUnleash.isEnabled(HENT_ENHETER_FRA_AD_OG_LOGG_DIFF)).thenReturn(true)
        `when`(axsysClient.hentTilganger(navIdent)).thenReturn(listOf(axsysEnhet))
        `when`(microsoftGraphClient.hentAdGrupper()).thenReturn(setOf(adGroup))

        val result = enhetService.hentTilganger(navIdent)

        assertEquals(1, result.size)
        assertEquals(EnhetId.of("1234"), result[0]?.enhetId)
        verify(microsoftGraphClient).hentAdGrupper()
    }

    @Test
    fun `tilEnhetId extracts correct EnhetId from AD group name`() {
        val result = EnhetService.tilEnhetId("0000-GA-ENHET_1234")
        assertEquals("1234", result.get())
    }

    @Test
    fun `tilEnhetId handles lowercase input correctly`() {
        val result = EnhetService.tilEnhetId("0000-ga-enhet_1234")
        assertEquals("1234", result.get())
    }

    @Test
    fun `tilValidertEnhetId throws exception for invalid length`() {
        val exception = assertThrows(EnhetService.NavEnhetIdValideringException::class.java) {
            EnhetService.tilEnhetId("0000-GA-ENHET_123")
        }

        assertTrue(exception.message!!.contains("Ugyldig lengde"))
    }

    @Test
    fun `tilValidertEnhetId throws exception for non-numeric input`() {
        val exception = assertThrows(EnhetService.NavEnhetIdValideringException::class.java) {
            EnhetService.tilEnhetId("0000-GA-ENHET_123A")
        }

        assertTrue(exception.message!!.contains("Ugyldige tegn"))
    }
}