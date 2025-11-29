package desarrollador.api.controllers;

import desarrollador.api.services.ImagenServicio;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/imagenes")
public class ImagenControlador {
    private final ImagenServicio servicio;

    public ImagenControlador(ImagenServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subir(@RequestPart("imagen") MultipartFile imagen,
                                   @RequestHeader(value = "X-Base-Url", required = false) String baseUrl) {
        try {
            String urlBase = baseUrl != null ? baseUrl : "";
            String url = servicio.guardar(imagen, urlBase);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al subir imagen");
        }
    }

    @GetMapping("/{archivo}")
    public ResponseEntity<Resource> obtener(@PathVariable String archivo) {
        Resource r = servicio.obtener(archivo);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(r);
    }
}
