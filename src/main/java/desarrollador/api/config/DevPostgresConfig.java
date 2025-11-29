package desarrollador.api.config;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
@Profile("dev")
public class DevPostgresConfig {
    @Bean
    public DataSource dataSource() throws Exception {
        EmbeddedPostgres pg = EmbeddedPostgres.builder().setPort(0).start();
        return pg.getPostgresDatabase();
    }
}
