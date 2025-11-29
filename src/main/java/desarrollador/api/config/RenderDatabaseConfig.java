package desarrollador.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("render")
public class RenderDatabaseConfig {
    @Bean
    public DataSource dataSource(Environment env) {
        String jdbc = env.getProperty("JDBC_DATABASE_URL");
        String dbUrl = env.getProperty("DATABASE_URL");

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");

        if (jdbc != null && !jdbc.isBlank()) {
            ds.setJdbcUrl(jdbc);
            String user = env.getProperty("DB_USER");
            String pass = env.getProperty("DB_PASS");
            if (user != null) ds.setUsername(user);
            if (pass != null) ds.setPassword(pass);
            return ds;
        }

        if (dbUrl != null && dbUrl.startsWith("postgres://")) {
            URI uri = URI.create(dbUrl);
            String username = uri.getUserInfo().split(":")[0];
            String password = uri.getUserInfo().split(":")[1];
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
            ds.setJdbcUrl(jdbcUrl);
            ds.setUsername(username);
            ds.setPassword(password);
            return ds;
        }

        if (dbUrl != null && dbUrl.startsWith("jdbc:")) {
            ds.setJdbcUrl(dbUrl);
            ds.setUsername(env.getProperty("DB_USER"));
            ds.setPassword(env.getProperty("DB_PASS"));
            return ds;
        }

        throw new IllegalStateException("No se encontraron variables de entorno de Base de Datos para Render");
    }
}
