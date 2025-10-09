package no.nav.veilarbveileder.service

import io.getunleash.DefaultUnleash
import no.nav.common.auth.context.AuthContextHolder
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysEnhet
import no.nav.common.client.msgraph.AdGroupData
import no.nav.common.client.msgraph.AdGroupFilter
import no.nav.common.client.msgraph.MsGraphClient
import no.nav.common.client.msgraph.UserData
import no.nav.common.client.norg2.Enhet
import no.nav.common.client.norg2.Norg2Client
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient
import no.nav.common.types.identer.AzureObjectId
import no.nav.common.types.identer.EnhetId
import no.nav.common.types.identer.NavIdent
import no.nav.veilarbveileder.config.EnvironmentProperties
import no.nav.veilarbveileder.service.EnhetService.Companion.AD_GRUPPE_ENHET_PREFIKS
import no.nav.veilarbveileder.utils.HENT_ENHETER_FRA_AD_OG_LOGG_DIFF
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*


@ExtendWith(MockitoExtension::class)
class EnhetServiceTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    private lateinit var norg2Client: Norg2Client

    @Mock(strictness = Mock.Strictness.LENIENT)
    private lateinit var axsysClient: AxsysClient

    @Mock(strictness = Mock.Strictness.LENIENT)
    private lateinit var msGraphClient: MsGraphClient

    @Mock(strictness = Mock.Strictness.LENIENT)
    private lateinit var azureAdMachineToMachineTokenClient: AzureAdMachineToMachineTokenClient

    @Mock(strictness = Mock.Strictness.LENIENT)
    private lateinit var azureAdOnBehalfOfTokenClient: AzureAdOnBehalfOfTokenClient

    @Mock(strictness = Mock.Strictness.LENIENT)
    private lateinit var authContextHolder: AuthContextHolder

    @Mock(strictness = Mock.Strictness.LENIENT)
    private lateinit var environmentProperties: EnvironmentProperties

    @Mock(strictness = Mock.Strictness.LENIENT)
    private lateinit var defaultUnleash: DefaultUnleash

    private lateinit var enhetService: EnhetService

    companion object {
        const val TEST_M2M_TOKEN = "m2m-token"
        const val TEST_OBO_TOKEN = "obo-token"
        const val TEST_SCOPED_OBO_TOKEN = "scoped-obo-token"
        const val TEST_NAV_IDENT = "Z998877"
    }

    @BeforeEach
    fun setup() {
        `when`(defaultUnleash.isEnabled(HENT_ENHETER_FRA_AD_OG_LOGG_DIFF)).thenReturn(true)
        `when`(azureAdMachineToMachineTokenClient.createMachineToMachineToken(any())).thenReturn(TEST_M2M_TOKEN)
        `when`(authContextHolder.requireIdTokenString()).thenReturn(TEST_OBO_TOKEN)
        `when`(azureAdOnBehalfOfTokenClient.exchangeOnBehalfOfToken(any(), eq(TEST_OBO_TOKEN))).thenReturn(
            TEST_SCOPED_OBO_TOKEN
        )

        enhetService = EnhetService(
            norg2Client,
            axsysClient,
            msGraphClient,
            azureAdMachineToMachineTokenClient,
            azureAdOnBehalfOfTokenClient,
            authContextHolder,
            environmentProperties,
            defaultUnleash
        )
    }

    @Test
    fun `hentEnhet returnerer forventet enhet`() {
        val enhetId = EnhetId.of("1234")
        val enhet = Enhet().setEnhetNr("1234").setNavn("NAV Test")

        `when`(norg2Client.hentEnhet("1234")).thenReturn(enhet)

        val result = enhetService.hentEnhet(enhetId)

        assertTrue(result.isPresent)
        assertEquals(EnhetId.of("1234"), result.get().enhetId)
        assertEquals("NAV Test", result.get().navn)
    }

    @Test
    fun `hentEnhet returnerer tom optional`() {
        val enhetId = EnhetId.of("1234")

        `when`(norg2Client.hentEnhet("1234")).thenThrow(RuntimeException("Not found"))

        val result = enhetService.hentEnhet(enhetId)

        assertTrue(result.isEmpty)
    }

    @Test
    fun `alleEnheter returnerer forventede porteføljeenheter`() {
        val enhet1 = Enhet().setEnhetNr("1234").setNavn("Nav-kontor 1")
        val enhet2 = Enhet().setEnhetNr("5678").setNavn("Nav-kontor 2")

        `when`(norg2Client.alleAktiveEnheter()).thenReturn(listOf(enhet1, enhet2))

        val result = enhetService.alleEnheter()

        assertEquals(2, result.size)
        assertEquals(EnhetId.of("1234"), result[0]?.enhetId)
        assertEquals("Nav-kontor 1", result[0]?.navn)
        assertEquals(EnhetId.of("5678"), result[1]?.enhetId)
        assertEquals("Nav-kontor 2", result[1]?.navn)
    }

    @Test
    fun `veilederePaEnhet returnerer forventede veiledere på enhet`() {
        val enhetId = EnhetId.of("1234")
        val userData1 = UserData().apply { onPremisesSamAccountName = "A123456" }
        val userData2 = UserData().apply { onPremisesSamAccountName = "B789012" }

        `when`(msGraphClient.hentUserDataForGroup(eq(TEST_M2M_TOKEN), eq(enhetId))).thenReturn(
            listOf(
                userData1,
                userData2
            )
        )

        val result = enhetService.veilederePaEnhet(enhetId)

        assertEquals(2, result?.size)
        assertEquals("A123456", result?.get(0)?.get())
        assertEquals("B789012", result?.get(1)?.get())
    }

    @Test
    fun `hentTilganger returnerer forventede tilganger for veileder`() {
        val adGroup = AdGroupData(AzureObjectId.of(UUID.randomUUID().toString()), "${AD_GRUPPE_ENHET_PREFIKS}1234")
        val axsysEnhet = AxsysEnhet().setEnhetId(EnhetId.of("1234")).setNavn("Nav Oslo")

        `when`(axsysClient.hentTilganger(NavIdent.of(TEST_NAV_IDENT))).thenReturn(listOf(axsysEnhet))
        `when`(
            msGraphClient.hentAdGroupsForUser(
                TEST_SCOPED_OBO_TOKEN,
                AdGroupFilter.ENHET
            )
        ).thenReturn(listOf(adGroup))

        val result = enhetService.hentTilganger(NavIdent.of(TEST_NAV_IDENT))

        assertEquals(1, result.size)
        assertEquals(EnhetId.of("1234"), result[0]?.enhetId)
    }

    @Test
    fun `tilEnhetId henter ut EnhetId fra AD-gruppe navn`() {
        val result = EnhetService.tilEnhetId("0000-GA-ENHET_1234")
        assertEquals("1234", result.get())
    }

    @Test
    fun `tilEnhetId hådnterer lowercase input korrekt`() {
        val result = EnhetService.tilEnhetId("0000-ga-enhet_1234")
        assertEquals("1234", result.get())
    }

    @Test
    fun `tilValidertEnhetId kaster exception dersom ugyldig lengde på enhet-ID`() {
        val exception = assertThrows(EnhetService.NavEnhetIdValideringException::class.java) {
            EnhetService.tilEnhetId("0000-GA-ENHET_123")
        }

        assertTrue(exception.message!!.contains("Ugyldig lengde"))
    }

    @Test
    fun `tilValidertEnhetId kaster exception for ikke-numerisk enhet-ID`() {
        val exception = assertThrows(EnhetService.NavEnhetIdValideringException::class.java) {
            EnhetService.tilEnhetId("0000-GA-ENHET_123A")
        }

        assertTrue(exception.message!!.contains("Ugyldige tegn"))
    }
}