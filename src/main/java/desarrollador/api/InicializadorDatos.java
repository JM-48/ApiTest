package desarrollador.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import desarrollador.api.models.Producto;
import desarrollador.api.models.Profile;
import desarrollador.api.models.Role;
import desarrollador.api.models.User;
import desarrollador.api.repositories.UserRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import desarrollador.api.repositories.ProductoRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class InicializadorDatos {
    private final ProductoRepositorio productoRepositorio;
    private final ObjectMapper mapper = new ObjectMapper();
    private final UserRepositorio userRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final Logger log = LoggerFactory.getLogger(InicializadorDatos.class);

    public InicializadorDatos(ProductoRepositorio productoRepositorio,
                              UserRepositorio userRepositorio,
                              PasswordEncoder passwordEncoder) {
        this.productoRepositorio = productoRepositorio;
        this.userRepositorio = userRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void cargar() {
        try {
            cargarAdminSiNoExiste();
        } catch (Exception e) {
            log.warn("Fallo en carga inicial", e);
        }
    }

    private void cargarAdminSiNoExiste() {
        if (userRepositorio.existsByEmailIgnoreCase("admin@catalogo.com")) return;
        User u = new User();
        u.setEmail("admin@catalogo.com");
        u.setPasswordHash(passwordEncoder.encode("admin123"));
        u.setRole(Role.ADMIN);
        Profile p = new Profile();
        p.setNombre("Admin");
        u.setProfile(p);
        userRepositorio.save(u);
        log.info("Admin creado");
    }

    private JsonNode leerJson(String rootFileName, String classpathFile) throws IOException {
        Path rootPath = Path.of("..","" + rootFileName);
        if (Files.exists(rootPath)) {
            byte[] bytes = Files.readAllBytes(rootPath);
            return mapper.readTree(bytes);
        }
        try {
            return mapper.readTree(Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathFile));
        } catch (Exception e) {
            return null;
        }
    }
}
