package desarrollador.api.controllers;

import desarrollador.api.dto.ProductoDTO;
import desarrollador.api.models.Categoria;
import desarrollador.api.models.Producto;
import desarrollador.api.repositories.CategoriaRepositorio;
import desarrollador.api.services.ImagenServicio;
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
@RequestMapping("/productos")
public class ProductoControlador {
    private final ProductoServicio servicio;
    private final ImagenServicio imagenServicio;
    private final CategoriaRepositorio categoriaRepositorio;

    public ProductoControlador(ProductoServicio servicio, ImagenServicio imagenServicio, CategoriaRepositorio categoriaRepositorio) {
        this.servicio = servicio;
        this.imagenServicio = imagenServicio;
        this.categoriaRepositorio = categoriaRepositorio;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear producto con imagen (multipart/form-data)")
    public ResponseEntity<?> crearMultipart(@RequestParam String nombre,
                                            @RequestParam(required = false) String descripcion,
                                            @RequestParam double precio,
                                            @RequestParam String tipo,
                                            @RequestParam(required = false) Integer stock,
                                            @RequestPart(required = false) MultipartFile imagen,
                                            @RequestHeader(value = "X-Base-Url", required = false) String baseUrl) {
        try {
            Producto p = new Producto();
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            p.setPrecio(precio);
            p.setStock(stock);
            if (imagen != null) {
                String urlBase = baseUrl != null ? baseUrl : "";
                String url = imagenServicio.guardar(imagen, urlBase);
                p.setImagenUrl(url);
            }
            Long categoriaId = categoriaRepositorio.findByNombreIgnoreCase(tipo)
                    .map(Categoria::getId)
                    .orElseGet(() -> {
                        Categoria c = new Categoria();
                        c.setNombre(tipo);
                        Categoria guardada = categoriaRepositorio.save(c);
                        return guardada.getId();
                    });
            Producto creado = servicio.crear(p, categoriaId);
            return ResponseEntity.created(URI.create("/productos/" + creado.getId())).body(ProductoDTO.fromEntity(creado));
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
