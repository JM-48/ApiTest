package desarrollador.api.controllers;

import desarrollador.api.dto.CheckoutDtos;
import desarrollador.api.dto.OrdenDTO;
import desarrollador.api.models.*;
import desarrollador.api.services.OrdenServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/checkout")
@SecurityRequirement(name = "bearerAuth")
public class CheckoutControlador {
    private final OrdenServicio ordenServicio;
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CheckoutControlador.class);

    public CheckoutControlador(OrdenServicio ordenServicio) {
        this.ordenServicio = ordenServicio;
    }

    @PostMapping(consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Iniciar checkout y marcar orden como pendiente")
    public ResponseEntity<?> checkout(Authentication auth, @RequestBody CheckoutDtos.CheckoutRequest req) {
        String requestId = UUID.randomUUID().toString();
        try {
            List<String> details = new ArrayList<>();
            if (req.items == null || req.items.isEmpty()) {
                details.add("items requerido y no vacío");
            } else {
                for (int i = 0; i < req.items.size(); i++) {
                    CheckoutDtos.CheckoutItem it = req.items.get(i);
                    if (it.cantidad < 1) details.add("items[" + i + "].cantidad debe ser >= 1");
                    if (it.precioUnitario < 0) details.add("items[" + i + "].precioUnitario debe ser >= 0");
                    if (it.nombre == null || it.nombre.isBlank()) details.add("items[" + i + "].nombre requerido");
                }
                double sum = req.items.stream().mapToDouble(it -> it.precioUnitario * it.cantidad).sum();
                double diff = Math.abs(sum - req.total);
                if (diff > 0.01) {
                    details.add("total no coincide con suma de items");
                }
            }
            if (req.destinatario == null || req.destinatario.isBlank()) details.add("destinatario requerido");
            if (req.direccion == null || req.direccion.isBlank()) details.add("direccion requerida");
            if (req.region == null || req.region.isBlank()) details.add("region requerida");
            if (req.ciudad == null || req.ciudad.isBlank()) details.add("ciudad requerida");
            if (req.codigoPostal == null || req.codigoPostal.isBlank()) details.add("codigoPostal requerido");
            if (req.metodoEnvio == null || (!req.metodoEnvio.equals("domicilio") && !req.metodoEnvio.equals("retiro"))) {
                details.add("metodoEnvio inválido (domicilio|retiro)");
            }
            if (req.metodoPago == null || (!req.metodoPago.equals("local") && !req.metodoPago.equals("tarjeta"))) {
                details.add("metodoPago inválido (local|tarjeta)");
            }
            User user = ordenServicio.getUserByEmail(auth.getName());
            String telefono = user.getProfile() == null ? null : user.getProfile().getTelefono();
            if (telefono == null || telefono.isBlank()) {
                details.add("telefono faltante en perfil del usuario");
            }
            if (!details.isEmpty()) {
                log.info("checkout validation failed requestId={} user={} details={}", requestId, user.getEmail(), details);
                return ResponseEntity.status(422).body(Map.of("message", "Validation error", "details", details));
            }
            Direccion d = new Direccion();
            d.setUser(user);
            d.setNombreDestinatario(req.destinatario);
            d.setTelefono(telefono);
            d.setDireccion(req.direccion);
            d.setRegion(req.region);
            d.setCiudad(req.ciudad);
            d.setCodigoPostal(req.codigoPostal);
            d = ordenServicio.crearDireccion(user, d);
            Orden orden = ordenServicio.crearOrden(user, d, req.total);
            for (CheckoutDtos.CheckoutItem it : req.items) {
                Producto p = ordenServicio.resolverProducto(it.productoId, it.nombre);
                if (p == null) {
                    log.info("checkout product not found requestId={} user={} producto={}/{}", requestId, user.getEmail(), it.productoId, it.nombre);
                    return ResponseEntity.status(422).body(Map.of("message", "Validation error", "details", List.of("Producto no encontrado: " + (it.productoId == null ? it.nombre : it.productoId))));
                }
                ordenServicio.agregarItemConPrecio(orden, p, it.cantidad, it.precioUnitario);
            }
            orden = ordenServicio.checkout(orden);
            List<DetalleOrden> items = ordenServicio.listarItems(orden);
            OrdenDTO dto = OrdenDTO.fromEntity(orden, items);
            log.info("checkout success requestId={} user={} ordenId={} total={}", requestId, user.getEmail(), dto.getId(), dto.getTotal());
            return ResponseEntity.status(201).body(Map.of(
                    "id", dto.getId(),
                    "status", "PENDING",
                    "total", dto.getTotal(),
                    "fechaPedido", dto.getFechaPedido(),
                    "items", req.items,
                    "createdAt", orden.getCreatedAt() == null ? java.time.Instant.now().toString() : orden.getCreatedAt().toString()
            ));
        } catch (Exception e) {
            log.error("checkout error requestId={} message={}", requestId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(path = "/{ordenId}/confirm", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
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
        if (orden.getStatus() == OrderStatus.PAID) {
            return ResponseEntity.status(409).body(Map.of("message", "Order already confirmed"));
        }
        Compra compra = ordenServicio.confirmarCompra(orden, referencia);
        return ResponseEntity.ok(Map.of(
                "ordenId", orden.getId(),
                "estado", "PAID",
                "referenciaPago", referencia
        ));
    }
}
