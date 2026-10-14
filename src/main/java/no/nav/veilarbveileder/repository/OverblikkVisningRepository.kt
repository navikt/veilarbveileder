package no.nav.veilarbveileder.repository

import no.nav.common.types.identer.NavIdent
import no.nav.veilarbveileder.domain.OverblikkVisning
import no.nav.veilarbveileder.domain.OverblikkVisningAlternativ
import no.nav.veilarbveileder.repository.OverblikkVisningTabell.ID
import no.nav.veilarbveileder.repository.OverblikkVisningTabell.NAVN
import no.nav.veilarbveileder.repository.OverblikkVisningTabell.SIST_ENDRET_TIDSPUNKT
import no.nav.veilarbveileder.repository.OverblikkVisningTabell.VEILEDER_ID
import no.nav.veilarbveileder.repository.OverblikkVisningTabell.VISNING
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class OverblikkVisningRepository(private val jdbcClient: JdbcClient) {

    fun hent(veilederId: NavIdent): OverblikkVisning? = jdbcClient
        .sql("SELECT $ID, $VISNING FROM $NAVN WHERE $VEILEDER_ID = :veilederId")
        .param("veilederId", veilederId.get())
        .query { rs, _ ->
            val databaseVisning = OverblikkVisningDb(
                id = rs.getObject(ID, UUID::class.java),
                visning = (rs.getArray(VISNING).array as Array<*>)
                    .map { OverblikkVisningAlternativDb.valueOf(it as String) }
            )
            OverblikkVisningDbMapper.tilDomene(databaseVisning)
        }
        .optional()
        .orElse(null)

    fun lagre(veilederId: NavIdent, visning: List<OverblikkVisningAlternativ>) {
        val databaseVisning = visning.map { OverblikkVisningDbMapper.tilDb(it).name }.toTypedArray()
        val eksisterendeVisning = hent(veilederId)
        if (eksisterendeVisning != null) {
            jdbcClient
                .sql(
                    """
                    UPDATE $NAVN
                    SET $VISNING = :visning, $SIST_ENDRET_TIDSPUNKT = CURRENT_TIMESTAMP
                    WHERE $ID = :id
                    """.trimIndent()
                )
                .param("id", eksisterendeVisning.id)
                .param("visning", databaseVisning)
                .update()
        } else {
            jdbcClient
                .sql("INSERT INTO $NAVN ($VEILEDER_ID, $VISNING) VALUES (:veilederId, :visning)")
                .param("veilederId", veilederId.get())
                .param("visning", databaseVisning)
                .update()
        }
    }

    fun slett(veilederId: NavIdent) {
        jdbcClient
            .sql("DELETE FROM $NAVN WHERE $VEILEDER_ID = :veilederId")
            .param("veilederId", veilederId.get())
            .update()
    }
}
