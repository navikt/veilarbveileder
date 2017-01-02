package no.nav.fo.config;

import no.nav.sbl.dialogarena.common.integrasjon.utils.SQL;
import no.nav.sbl.dialogarena.types.Pingable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jndi.JndiTemplate;
import no.nav.sbl.dialogarena.common.integrasjon.utils.RowMapper;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@Configuration
public class DatabaseConfig {
    /*@Bean
    public DataSource oracleDataSource() throws ClassNotFoundException, NamingException {
        return new JndiTemplate().lookup("java:/jboss/datasources/veilarbportefolje", DataSource.class);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) throws NamingException, SQLException, IOException {
        return new JdbcTemplate(dataSource);
    }*/

//    @Bean
//    public DataSourceTransactionManager transactionManager(DataSource dataSource) throws IOException, SQLException {
//        return new DataSourceTransactionManager(dataSource);
//    }

   /* @Bean
    public Pingable dbPinger(final DataSource ds) {
        return () -> {
            try {
                SQL.query(ds, new RowMapper.IntMapper(), "select count(1) from dual");
                return Pingable.Ping.lyktes("DATABASE");
            } catch (Exception e) {
                return Pingable.Ping.feilet("DATABASE", e);
            }
        };
    }*/

}
