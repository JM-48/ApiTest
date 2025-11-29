package desarrollador.api.controllers;

import desarrollador.api.models.Producto;
import desarrollador.api.services.ImagenServicio;
import desarrollador.api.services.ProductoServicio;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoControlador {
    private final ProductoServicio servicio;
    private final ImagenServicio imagenServicio;

    public ProductoControlador(ProductoServicio servicio, ImagenServicio imagenServicio) {
        this.servicio = servicio;
        this.imagenServicio = imagenServicio;
    }

    @GetMapping
    public List<Producto> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(servicio.obtener(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Producto no encontrado");
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearMultipart(@RequestParam String nombre,
                                            @RequestParam(required = false) String descripcion,
                                            @RequestParam double precio,
                                            @RequestParam Long categoriaId,
                                            @RequestPart(required = false) MultipartFile imagen,
                                            @RequestHeader(value = "X-Base-Url", required = false) String baseUrl) {
        try {
            Producto p = new Producto();
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            p.setPrecio(precio);
            if (imagen != null) {
                String urlBase = baseUrl != null ? baseUrl : "";
                String url = imagenServicio.guardar(imagen, urlBase);
                p.setImagenUrl(url);
            }
            Producto creado = servicio.crear(p, categoriaId);
            return ResponseEntity.created(URI.create("/productos/" + creado.getId())).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> crearJson(@RequestBody Producto payload,
                                       @RequestParam Long categoriaId) {
        try {
            Producto creado = servicio.crear(payload, categoriaId);
            return ResponseEntity.created(URI.create("/productos/" + creado.getId())).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody Producto cambios,
                                        @RequestParam(required = false) Long categoriaId) {
        try {
            return ResponseEntity.ok(servicio.actualizar(id, cambios, categoriaId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            servicio.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Producto no encontrado");
        }
    }
}
