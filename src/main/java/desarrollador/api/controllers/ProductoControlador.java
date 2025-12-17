package desarrollador.api.controllers;

import desarrollador.api.dto.ProductoDTO;
import desarrollador.api.models.Producto;
import desarrollador.api.services.ImagenCloudService;
import desarrollador.api.services.ProductoServicio;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoControlador {
    private final ProductoServicio servicio;
    private final ImagenCloudService imagenCloudService;

    public ProductoControlador(ProductoServicio servicio, ImagenCloudService imagenCloudService) {
        this.servicio = servicio;
        this.imagenCloudService = imagenCloudService;
    }

    @GetMapping
    @Operation(summary = "Listar productos")
    public List<ProductoDTO> listar() {
        return servicio.listar().stream().map(ProductoDTO::fromEntity).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por id")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            Producto p = servicio.obtener(id);
            return ResponseEntity.ok(ProductoDTO.fromEntity(p));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Producto no encontrado");
        }
    }

    @GetMapping("/{id}/precio")
    @Operation(summary = "Detalle de precio: original e IVA")
    public ResponseEntity<?> precio(@PathVariable Long id) {
        try {
            Producto p = servicio.obtener(id);
            double conIva = p.getPrecio();
            double original = Math.round((conIva / 1.19) * 100.0) / 100.0;
            double ivaMonto = Math.round((conIva - original) * 100.0) / 100.0;
            return ResponseEntity.ok(java.util.Map.of(
                    "precioOriginal", original,
                    "precioConIva", conIva,
                    "ivaPorcentaje", 0.19,
                    "ivaMonto", ivaMonto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Producto no encontrado");
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','PROD_AD','VENDEDOR')")
    @Operation(summary = "Crear producto con imagen (multipart/form-data)")
    public ResponseEntity<?> crearMultipart(@RequestParam String nombre,
                                            @RequestParam(required = false) String descripcion,
                                            @RequestParam double precio,
                                            @RequestParam String tipo,
                                            @RequestParam(required = false) Integer stock,
                                            @RequestPart(required = false) MultipartFile imagen) {
        try {
            Producto p = new Producto();
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            p.setPrecio(precio);
            p.setStock(stock);
            p.setCategoriaNombre(tipo);
            if (imagen != null) {
                String url = imagenCloudService.subirImagen(imagen);
                p.setImagenUrl(url);
            }
            Producto creado = servicio.crear(p);
            return ResponseEntity.created(URI.create("/productos/" + creado.getId())).body(ProductoDTO.fromEntity(creado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','PROD_AD')")
    public ResponseEntity<?> crearJson(@jakarta.validation.Valid @RequestBody Producto payload) {
        try {
            Producto creado = servicio.crear(payload);
            return ResponseEntity.created(URI.create("/productos/" + creado.getId())).body(ProductoDTO.fromEntity(creado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','PROD_AD','VENDEDOR')")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @jakarta.validation.Valid @RequestBody Producto cambios) {
        try {
            return ResponseEntity.ok(servicio.actualizar(id, cambios));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping(path = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen de producto a Cloudinary")
    public ResponseEntity<?> subirImagen(@PathVariable Long id, @RequestPart("imagen") MultipartFile imagen) {
        try {
            String url = imagenCloudService.subirImagen(imagen);
            servicio.actualizarImagen(id, url);
            return ResponseEntity.ok(java.util.Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping(path = "/{id}/imagen", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Actualizar imagen de producto con URL existente")
    public ResponseEntity<?> actualizarImagenPorUrl(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        try {
            String url = body == null ? null : body.get("url");
            if (url == null || url.isBlank()) {
                return ResponseEntity.badRequest().body("url requerida");
            }
            servicio.actualizarImagen(id, url);
            return ResponseEntity.ok(java.util.Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN','PROD_AD','VENDEDOR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            servicio.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Producto no encontrado");
        }
    }
}
