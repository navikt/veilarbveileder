package no.nav.veilarbveileder.repository

import no.nav.common.types.identer.NavIdent
import no.nav.veilarbveileder.domain.OverblikkVisning
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class OverblikkVisningRepository(private val jdbcClient: JdbcClient) {

    fun hent(veilederId: NavIdent): OverblikkVisning? = jdbcClient
        .sql("SELECT OVERBLIKK_VISNING_ID, VISNING FROM OVERBLIKK_VISNING WHERE VEILEDER_ID = :veilederId")
        .param("veilederId", veilederId.get())
        .query { rs, _ ->
            OverblikkVisning(
                id = rs.getObject("OVERBLIKK_VISNING_ID", UUID::class.java),
                visning = (rs.getArray("VISNING").array as Array<*>).map { it as String }
            )
        }
        .optional()
        .orElse(null)

    fun lagre(veilederId: NavIdent, visning: List<String>) {
        val eksisterendeVisning = hent(veilederId)
        if (eksisterendeVisning != null) {
            jdbcClient
                .sql(
                    """
                    UPDATE OVERBLIKK_VISNING
                    SET VISNING = :visning, SIST_ENDRET_TIDSPUNKT = CURRENT_TIMESTAMP
                    WHERE OVERBLIKK_VISNING_ID = :id
                    """.trimIndent()
                )
                .param("id", eksisterendeVisning.id)
                .param("visning", visning.toTypedArray())
                .update()
        } else {
            jdbcClient
                .sql("INSERT INTO OVERBLIKK_VISNING (VEILEDER_ID, VISNING) VALUES (:veilederId, :visning)")
                .param("veilederId", veilederId.get())
                .param("visning", visning.toTypedArray())
                .update()
        }
    }

    fun slett(veilederId: NavIdent) {
        jdbcClient
            .sql("DELETE FROM OVERBLIKK_VISNING WHERE VEILEDER_ID = :veilederId")
            .param("veilederId", veilederId.get())
            .update()
    }
}
