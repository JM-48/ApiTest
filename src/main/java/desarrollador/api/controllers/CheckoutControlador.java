package desarrollador.api.controllers;

import desarrollador.api.models.*;
import desarrollador.api.services.OrdenServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkout")
@SecurityRequirement(name = "bearerAuth")
public class CheckoutControlador {
    private final OrdenServicio ordenServicio;

    public CheckoutControlador(OrdenServicio ordenServicio) {
        this.ordenServicio = ordenServicio;
    }

    @PostMapping
    @Operation(summary = "Iniciar checkout y marcar orden como pendiente")
    public ResponseEntity<?> checkout(Authentication auth, @RequestBody Direccion direccion) {
        User user = ordenServicio.getUserByEmail(auth.getName());
        Direccion d = ordenServicio.crearDireccion(user, direccion);
        Orden cart = ordenServicio.getOrCreateCart(user, d);
        cart = ordenServicio.checkout(cart);
        java.util.List<DetalleOrden> items = ordenServicio.listarItems(cart);
        return ResponseEntity.ok(desarrollador.api.dto.OrdenDTO.fromEntity(cart, items));
    }

    @PostMapping("/{ordenId}/confirm")
    @Operation(summary = "Confirmar compra de una orden pendiente")
    public ResponseEntity<?> confirmar(Authentication auth, @PathVariable Long ordenId, @RequestBody Map<String, String> body) {
        String referencia = body == null ? null : body.getOrDefault("referenciaPago", null);
        if (referencia == null || referencia.isBlank()) {
            return ResponseEntity.badRequest().body("referenciaPago requerida");
        }
        Orden orden = ordenServicio.obtener(ordenId);
        if (!orden.getUser().getEmail().equalsIgnoreCase(auth.getName())) {
            return ResponseEntity.status(403).body("forbidden");
        }
        Compra compra = ordenServicio.confirmarCompra(orden, referencia);
        return ResponseEntity.ok(desarrollador.api.dto.CompraDTO.fromEntity(compra));
    }
}
