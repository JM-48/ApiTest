package desarrollador.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaFixer {
    private final JdbcTemplate jdbc;
    private final Logger log = LoggerFactory.getLogger(SchemaFixer.class);

    public SchemaFixer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fix() {
        try {
            jdbc.execute("ALTER TABLE producto DROP COLUMN IF EXISTS categoria_id CASCADE");
        } catch (Exception e) {
            log.warn("No se pudo ajustar columna categoria_id", e);
        }
        try {
            jdbc.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
            jdbc.execute("ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('ADMIN','USER_AD','PROD_AD','VENDEDOR','CLIENT','USER'))");
        } catch (Exception e) {
            log.warn("No se pudo ajustar constraint de role", e);
        }
    }
}
