package no.nav.veilarbveileder.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.jdbc.core.simple.JdbcClient
import javax.sql.DataSource

@Configuration
class DatabaseConfig {
    @Bean(destroyMethod = "close")
    fun dataSource(
        @Value("\${DB_JDBC_URL}") jdbcUrl: String,
        @Value("\${DB_USERNAME}") username: String,
        @Value("\${DB_PASSWORD}") password: String
    ): HikariDataSource = HikariDataSource(
        HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
        }
    )

    @Bean(initMethod = "migrate")
    fun flyway(dataSource: DataSource): Flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()

    @Bean
    @DependsOn("flyway")
    fun jdbcClient(dataSource: DataSource): JdbcClient = JdbcClient.create(dataSource)
}
