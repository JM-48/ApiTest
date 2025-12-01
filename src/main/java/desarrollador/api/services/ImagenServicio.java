package desarrollador.api.services;

import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImagenServicio {
    private final Path uploadsDir = Path.of("uploads");
    private final Environment env;

    public ImagenServicio(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadsDir);
        } catch (IOException ignored) {
        }
    }

    public String guardar(MultipartFile imagen, String baseUrl) throws IOException {
        String original = imagen.getOriginalFilename();
        String extension = "";
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.'));
        }
        String nombre = UUID.randomUUID() + extension;
        Path destino = uploadsDir.resolve(nombre);
        Files.copy(imagen.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        String base = baseUrl;
        if (base == null || base.isBlank()) {
            String renderUrl = env.getProperty("RENDER_EXTERNAL_URL");
            if (renderUrl == null || renderUrl.isBlank()) {
                renderUrl = System.getenv("RENDER_EXTERNAL_URL");
            }
            base = renderUrl != null ? renderUrl : "";
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/uploads/" + nombre;
    }

    public Resource obtener(String archivo) {
        File f = uploadsDir.resolve(archivo).toFile();
        return new FileSystemResource(f);
    }
}
