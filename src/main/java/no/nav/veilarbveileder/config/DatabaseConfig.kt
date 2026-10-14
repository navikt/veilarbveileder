package no.nav.veilarbveileder.config

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseConfig {
    @Bean(initMethod = "migrate")
    @ConditionalOnProperty(name = ["DB_JDBC_URL"])
    fun flyway(
        @Value("\${DB_JDBC_URL}") jdbcUrl: String,
        @Value("\${DB_USERNAME}") username: String,
        @Value("\${DB_PASSWORD}") password: String
    ): Flyway = Flyway.configure()
        .dataSource(jdbcUrl, username, password)
        .locations("classpath:db/migration")
        .load()
}
