package desarrollador.api.controllers;

import desarrollador.api.services.ImagenCloudService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/imagenes")
public class ImagenControlador {
    private final ImagenCloudService imagenCloudService;

    public ImagenControlador(ImagenCloudService imagenCloudService) {
        this.imagenCloudService = imagenCloudService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen a Cloudinary y devolver URL")
    public ResponseEntity<?> subir(@RequestPart("imagen") MultipartFile imagen) {
        try {
            String url = imagenCloudService.subirImagen(imagen);
            return ResponseEntity.ok(java.util.Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
