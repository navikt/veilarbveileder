package no.nav.veilarbveileder.controller

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.nom.NomClient
import no.nav.common.client.norg2.Norg2Client
import no.nav.common.json.JsonUtils
import no.nav.veilarbveileder.client.MicrosoftGraphClient
import no.nav.veilarbveileder.config.ApplicationTestConfig
import no.nav.veilarbveileder.config.EnvironmentConfig
import no.nav.veilarbveileder.domain.Veileder
import no.nav.veilarbveileder.service.AuthService
import no.nav.veilarbveileder.service.EnhetService
import no.nav.veilarbveileder.service.VeilederOgEnhetServiceV2
import no.nav.veilarbveileder.service.VeilederService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootTest(
    classes = [
        ApplicationTestConfig::class,
        VeilederController::class,
        VeilederOgEnhetServiceV2::class,
        EnhetService::class,
        VeilederService::class,
        NomClient::class
    ]
)
@AutoConfigureMockMvc
@EnableWebMvc
@WireMockTest(httpPort = 8080)
class VeilederControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var authService: AuthService

    @MockBean
    lateinit var norg2Client: Norg2Client

    @MockBean
    lateinit var axsysClient: AxsysClient

    @MockBean
    lateinit var microsoftGraphClient: MicrosoftGraphClient

    @Autowired
    lateinit var nomClient: NomClient

    init {
        EnvironmentConfig.setup()
    }

    @Test
    fun `skal hente info om veileder`() {
        val veilederIdent = "Z1234"
        stubFor(
            post(urlMatching("/graphql"))
                .withRequestBody(
                    equalToJson(
                        "{" +
                                "  \"query\": \"query(\$identer: [String!]!) {    ressurser(where: { navIdenter: \$identer }){        id        ressurs {            navIdent            visningsNavn            fornavn            etternavn        }    }}\"," +
                                "  \"variables\": {" +
                                "    \"identer\": [\"$veilederIdent\"]" +
                                "  }" +
                                "}"
                    )
                )
                .willReturn(ok().withBodyFile("ressurser-enkel-response.json"))
        )

        val expectedResponseContent = JsonUtils.toJson(Veileder().apply {
            ident = veilederIdent
            fornavn = "F1234 M1234"
            etternavn = "E1234"
            navn = "E1234, F1234 M1234"
        })

        mockMvc.perform(
            get("/api/veileder/{identer}", veilederIdent).accept(MediaType.APPLICATION_JSON_VALUE)
        )
            .andExpect(status().isOk)
            .andExpect(content().json(expectedResponseContent, false))
    }

    @Test
    fun `skal hente info om veiledere`() {
        val veilederIdent1 = "Z1234"
        val veilederIdent2 = "Z5678"
        stubFor(
            post(urlMatching("/graphql"))
                .withRequestBody(
                    equalToJson(
                        "{" +
                                "  \"query\": \"query(\$identer: [String!]!) {    ressurser(where: { navIdenter: \$identer }){        id        ressurs {            navIdent            visningsNavn            fornavn            etternavn        }    }}\"," +
                                "  \"variables\": {" +
                                "    \"identer\": [\"$veilederIdent1\", \"$veilederIdent2\"]" +
                                "  }" +
                                "}"
                    )
                )
                .willReturn(ok().withBodyFile("ressurser-flere-response.json"))
        )

        val expectedResponseContent = JsonUtils.toJson(
            listOf(Veileder().apply {
                ident = veilederIdent1
                fornavn = "F1234 M1234"
                etternavn = "E1234"
                navn = "E1234, F1234 M1234"
            },
                Veileder().apply {
                    ident = veilederIdent2
                    fornavn = "F5678"
                    etternavn = "E5678"
                    navn = "E5678, F5678"
                })
        )

        mockMvc.perform(
            post("/api/veileder/list")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                    { "identer": ["$veilederIdent1", "$veilederIdent2"] }
                """.trimIndent())
        )
            .andExpect(status().isOk)
            .andExpect(content().json(expectedResponseContent, false))
    }
}
