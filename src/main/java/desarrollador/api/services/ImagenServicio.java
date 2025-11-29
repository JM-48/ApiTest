package desarrollador.api.services;

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

    public ImagenServicio() {
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
        return baseUrl + "/imagenes/" + nombre;
    }

    public Resource obtener(String archivo) {
        File f = uploadsDir.resolve(archivo).toFile();
        return new FileSystemResource(f);
    }
}
