package desarrollador.api.controllers;

import desarrollador.api.models.Categoria;
import desarrollador.api.services.CategoriaServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaControlador {
    private final CategoriaServicio servicio;

    public CategoriaControlador(CategoriaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Categoria> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(servicio.obtener(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Categoria no encontrada");
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Categoria categoria) {
        try {
            Categoria creada = servicio.crear(categoria);
            return ResponseEntity.created(URI.create("/categorias/" + creada.getId())).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Categoria cambios) {
        try {
            return ResponseEntity.ok(servicio.actualizar(id, cambios));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Categoria no encontrada");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            servicio.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Categoria no encontrada");
        }
    }
}
