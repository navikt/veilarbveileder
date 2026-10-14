package no.nav.veilarbveileder.controller

import no.nav.common.types.identer.NavIdent
import no.nav.veilarbveileder.config.ApplicationTestConfig
import no.nav.veilarbveileder.config.DatabaseConfig
import no.nav.veilarbveileder.config.EnvironmentConfig
import no.nav.veilarbveileder.repository.OverblikkVisningRepository
import no.nav.veilarbveileder.service.AuthService
import no.nav.veilarbveileder.service.OverblikkVisningService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.server.ResponseStatusException
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@SpringBootTest(
    classes = [
        ApplicationTestConfig::class,
        DatabaseConfig::class,
        OverblikkVisningController::class,
        OverblikkVisningService::class,
        OverblikkVisningRepository::class
    ]
)
@AutoConfigureMockMvc
@EnableWebMvc
@Testcontainers(disabledWithoutDocker = true)
class OverblikkVisningControllerTest {
    @Autowired
    lateinit var mockMvcTester: MockMvcTester

    @Autowired
    lateinit var authService: AuthService

    @Autowired
    lateinit var jdbcClient: JdbcClient

    init {
        EnvironmentConfig.setup()
    }

    @BeforeEach
    fun setup() {
        jdbcClient.sql("TRUNCATE OVERBLIKK_VISNING").update()
        reset(authService)
        `when`(authService.innloggetVeilederIdent).thenReturn(NavIdent.of(VEILEDER_IDENT))
    }

    @Test
    fun `skal returnere overblikkvisning naar overblikkvisning eksisterer`() {
        lagreOverblikkVisning("""{"overblikkVisning":["CV","Personalia"]}""")

        assertThat(hentOverblikkVisning())
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.overblikkVisning")
            .asArray()
            .containsExactlyInAnyOrder("CV", "Personalia")
    }

    @Test
    fun `skal returnere default respons naar ingen overblikkvisning eksisterer`() {
        assertThat(hentOverblikkVisning())
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.overblikkVisning")
            .asArray()
            .isEmpty()
    }

    @Test
    fun `skal lagre ny overblikkvisning`() {
        assertThat(lagreOverblikkVisning("""{"overblikkVisning":["CV","Personalia"]}"""))
            .hasStatus(HttpStatus.NO_CONTENT)
        assertThat(hentOverblikkVisningId().version()).isEqualTo(7)

        assertThat(hentOverblikkVisning())
            .bodyJson()
            .extractingPath("$.overblikkVisning")
            .asArray()
            .containsExactlyInAnyOrder("CV", "Personalia")
    }

    @Test
    fun `skal overskrive overblikkvisning dersom overblikkvisning eksisterer`() {
        lagreOverblikkVisning("""{"overblikkVisning":["CV","Personalia"]}""")
        val idFoerOppdatering = hentOverblikkVisningId()
        lagreOverblikkVisning("""{"overblikkVisning":["CV"]}""")

        assertThat(hentOverblikkVisning())
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.overblikkVisning")
            .asArray()
            .containsExactly("CV")
        assertThat(hentOverblikkVisningId()).isEqualTo(idFoerOppdatering)
    }

    @Test
    fun `skal returnere tom respons naar sletting er utfort dersom veileder har lagret overblikkvisning`() {
        lagreOverblikkVisning("""{"overblikkVisning":["CV","Personalia"]}""")

        assertThat(slettOverblikkVisning())
            .hasStatus(HttpStatus.NO_CONTENT)

        assertThat(hentOverblikkVisning())
            .bodyJson()
            .extractingPath("$.overblikkVisning")
            .asArray()
            .isEmpty()
    }

    @Test
    fun `skal returnere tom respons naar sletting er utfort dersom veileder ikke har lagret overblikkvisning`() {
        assertThat(slettOverblikkVisning())
            .hasStatus(HttpStatus.NO_CONTENT)
    }

    @Test
    fun `skal returnere feilmelding dersom input data er ulovlig`() {
        assertThat(lagreOverblikkVisning("""{"overblikkVisning":["FØØ","BÅR"]}"""))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `skal returnere 403 dersom veileder ikke har tilgang til Modia`() {
        doThrow(ResponseStatusException(HttpStatus.FORBIDDEN)).`when`(authService).sjekkTilgangTilModia()

        assertThat(hentOverblikkVisning()).hasStatus(HttpStatus.FORBIDDEN)
        assertThat(lagreOverblikkVisning("""{"overblikkVisning":["CV"]}""")).hasStatus(HttpStatus.FORBIDDEN)
        assertThat(slettOverblikkVisning()).hasStatus(HttpStatus.FORBIDDEN)
    }

    private fun hentOverblikkVisning() = mockMvcTester.get()
        .uri(URL)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()

    private fun lagreOverblikkVisning(json: String) = mockMvcTester.post()
        .uri(URL)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(json)
        .exchange()

    private fun slettOverblikkVisning() = mockMvcTester.delete()
        .uri(URL)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()

    private fun hentOverblikkVisningId() = jdbcClient
        .sql("SELECT OVERBLIKK_VISNING_ID FROM OVERBLIKK_VISNING WHERE VEILEDER_ID = :veilederId")
        .param("veilederId", VEILEDER_IDENT)
        .query { rs, _ -> rs.getObject("OVERBLIKK_VISNING_ID", java.util.UUID::class.java) }
        .single()

    companion object {
        private const val URL = "/api/overblikkvisning"
        private const val VEILEDER_IDENT = "Z999999"

        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:18-alpine")

        @DynamicPropertySource
        @JvmStatic
        fun databaseProperties(registry: DynamicPropertyRegistry) {
            registry.add("DB_JDBC_URL", postgres::getJdbcUrl)
            registry.add("DB_USERNAME", postgres::getUsername)
            registry.add("DB_PASSWORD", postgres::getPassword)
        }
    }
}
