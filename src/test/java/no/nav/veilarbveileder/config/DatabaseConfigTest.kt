package no.nav.veilarbveileder.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@Testcontainers(disabledWithoutDocker = true)
class DatabaseConfigTest {
    @Test
    fun `runs database migrations`() {
        val migrationResult = DatabaseConfig()
            .flyway(postgres.jdbcUrl, postgres.username, postgres.password)
            .migrate()

        assertThat(migrationResult.success).isTrue()
    }

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:18-alpine")
    }
}
