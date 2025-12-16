package desarrollador.api.controllers;

import desarrollador.api.models.*;
import desarrollador.api.services.OrdenServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@SecurityRequirement(name = "bearerAuth")
public class CarritoControlador {
    private final OrdenServicio ordenServicio;

    public CarritoControlador(OrdenServicio ordenServicio) {
        this.ordenServicio = ordenServicio;
    }

    @GetMapping
    @Operation(summary = "Obtener carrito actual del usuario")
    public ResponseEntity<?> getCart(Authentication auth) {
        User user = ordenServicio.getUserByEmail(auth.getName());
        Direccion d = direccionDesdePerfil(user);
        if (d == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "datos_envio_incompletos"));
        }
        d = ordenServicio.crearDireccion(user, d);
        Orden cart = ordenServicio.getOrCreateCart(user, d);
        List<DetalleOrden> items = ordenServicio.listarItems(cart);
        return ResponseEntity.ok(desarrollador.api.dto.OrdenDTO.fromEntity(cart, items));
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar producto al carrito")
    public ResponseEntity<?> addItem(Authentication auth, @RequestBody Map<String, Object> body) {
        User user = ordenServicio.getUserByEmail(auth.getName());
        Direccion d = direccionDesdePerfil(user);
        if (d == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "datos_envio_incompletos"));
        }
        d = ordenServicio.crearDireccion(user, d);
        Orden cart = ordenServicio.getOrCreateCart(user, d);
        Number pid = (Number) body.get("productoId");
        Number qty = (Number) body.get("cantidad");
        if (pid == null || qty == null) {
            return ResponseEntity.badRequest().body("productoId y cantidad requeridos");
        }
        cart = ordenServicio.agregarItem(cart, pid.longValue(), qty.intValue());
        List<DetalleOrden> items = ordenServicio.listarItems(cart);
        return ResponseEntity.ok(desarrollador.api.dto.OrdenDTO.fromEntity(cart, items));
    }

    @PutMapping("/items/{productoId}")
    @Operation(summary = "Actualizar cantidad de un producto en el carrito")
    public ResponseEntity<?> updateItem(Authentication auth, @PathVariable Long productoId, @RequestBody Map<String, Object> body) {
        User user = ordenServicio.getUserByEmail(auth.getName());
        Direccion d = direccionDesdePerfil(user);
        if (d == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "datos_envio_incompletos"));
        }
        d = ordenServicio.crearDireccion(user, d);
        Orden cart = ordenServicio.getOrCreateCart(user, d);
        Number qty = (Number) body.get("cantidad");
        if (qty == null) {
            return ResponseEntity.badRequest().body("cantidad requerida");
        }
        cart = ordenServicio.actualizarItem(cart, productoId, qty.intValue());
        List<DetalleOrden> items = ordenServicio.listarItems(cart);
        return ResponseEntity.ok(desarrollador.api.dto.OrdenDTO.fromEntity(cart, items));
    }

    @DeleteMapping("/items/{productoId}")
    @Operation(summary = "Eliminar producto del carrito")
    public ResponseEntity<?> removeItem(Authentication auth, @PathVariable Long productoId) {
        User user = ordenServicio.getUserByEmail(auth.getName());
        Direccion d = direccionDesdePerfil(user);
        if (d == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "datos_envio_incompletos"));
        }
        d = ordenServicio.crearDireccion(user, d);
        Orden cart = ordenServicio.getOrCreateCart(user, d);
        cart = ordenServicio.eliminarItem(cart, productoId);
        List<DetalleOrden> items = ordenServicio.listarItems(cart);
        return ResponseEntity.ok(desarrollador.api.dto.OrdenDTO.fromEntity(cart, items));
    }

    private Direccion direccionDesdePerfil(User user) {
        Profile p = user.getProfile();
        if (p == null) return null;
        if (p.getNombre() == null || p.getNombre().isBlank()) return null;
        if (p.getApellido() == null || p.getApellido().isBlank()) return null;
        if (p.getTelefono() == null || p.getTelefono().isBlank()) return null;
        if (p.getDireccion() == null || p.getDireccion().isBlank()) return null;
        if (p.getRegion() == null || p.getRegion().isBlank()) return null;
        if (p.getCiudad() == null || p.getCiudad().isBlank()) return null;
        if (p.getCodigoPostal() == null || p.getCodigoPostal().isBlank()) return null;
        Direccion d = new Direccion();
        d.setUser(user);
        d.setNombreDestinatario(p.getNombre() + " " + p.getApellido());
        d.setTelefono(p.getTelefono());
        d.setDireccion(p.getDireccion());
        d.setRegion(p.getRegion());
        d.setCiudad(p.getCiudad());
        d.setCodigoPostal(p.getCodigoPostal());
        return d;
    }
}
