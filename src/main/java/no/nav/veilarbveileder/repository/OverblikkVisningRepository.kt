package no.nav.veilarbveileder.repository

import no.nav.common.types.identer.NavIdent
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class OverblikkVisningRepository(private val jdbcClient: JdbcClient) {

    fun hent(veilederId: NavIdent): List<String>? = jdbcClient
        .sql("SELECT VISNING FROM OVERBLIKK_VISNING WHERE VEILEDER_ID = :veilederId")
        .param("veilederId", veilederId.get())
        .query { rs, _ -> (rs.getArray("VISNING").array as Array<*>).map { it as String } }
        .optional()
        .orElse(null)

    /**
     * Oppdaterer eksisterende visning for veilederen, eller oppretter en ny dersom ingen finnes.
     * Gjøres i én spørring slik at det ikke trengs en egen transaksjon.
     */
    fun lagre(veilederId: NavIdent, visning: List<String>) {
        jdbcClient
            .sql(
                """
                WITH oppdatert AS (
                    UPDATE OVERBLIKK_VISNING
                    SET VISNING = :visning, SIST_ENDRET_TIDSPUNKT = CURRENT_TIMESTAMP
                    WHERE VEILEDER_ID = :veilederId
                    RETURNING 1
                )
                INSERT INTO OVERBLIKK_VISNING (OVERBLIKK_VISNING_ID, VEILEDER_ID, VISNING)
                SELECT :id, :veilederId, :visning
                WHERE NOT EXISTS (SELECT 1 FROM oppdatert)
                """.trimIndent()
            )
            .param("id", UUID.randomUUID())
            .param("veilederId", veilederId.get())
            .param("visning", visning.toTypedArray())
            .update()
    }

    fun slett(veilederId: NavIdent) {
        jdbcClient
            .sql("DELETE FROM OVERBLIKK_VISNING WHERE VEILEDER_ID = :veilederId")
            .param("veilederId", veilederId.get())
            .update()
    }
}
