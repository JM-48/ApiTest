package desarrollador.api.controllers;

import desarrollador.api.models.Orden;
import desarrollador.api.models.User;
import desarrollador.api.repositories.OrdenRepositorio;
import desarrollador.api.services.OrdenServicio;
import desarrollador.api.dto.OrdenUpdateDTO;
import desarrollador.api.models.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@SecurityRequirement(name = "bearerAuth")
public class OrdenControlador {
    private final OrdenServicio ordenServicio;
    private final OrdenRepositorio ordenRepositorio;

    public OrdenControlador(OrdenServicio ordenServicio, OrdenRepositorio ordenRepositorio) {
        this.ordenServicio = ordenServicio;
        this.ordenRepositorio = ordenRepositorio;
    }

    @GetMapping
    @Operation(summary = "Listar órdenes del usuario autenticado")
    public ResponseEntity<?> misOrdenes(Authentication auth) {
        User user = ordenServicio.getUserByEmail(auth.getName());
        List<Orden> list = ordenServicio.listarPorUsuario(user);
        return ResponseEntity.ok(list.stream()
                .map(o -> desarrollador.api.dto.OrdenDTO.fromEntity(o, ordenServicio.listarItems(o)))
                .toList());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @Operation(summary = "Listar todas las órdenes (admin y vendedor)")
    public ResponseEntity<?> todas() {
        List<Orden> list = ordenRepositorio.findAll();
        return ResponseEntity.ok(list.stream()
                .map(o -> desarrollador.api.dto.OrdenDTO.fromEntity(o, ordenServicio.listarItems(o)))
                .toList());
    }

    @PatchMapping(path = "/{id}", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    @Operation(summary = "Editar orden (status, dirección, fechaPedido)")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody OrdenUpdateDTO req) {
        Orden orden = ordenServicio.obtener(id);
        if (req.status != null && !req.status.isBlank()) {
            OrderStatus nuevo;
            try {
                nuevo = OrderStatus.valueOf(req.status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(422).body(java.util.Map.of("message","Validation error","details", java.util.List.of("status inválido")));
            }
            if (orden.getStatus() == OrderStatus.PAID && nuevo != OrderStatus.PAID && nuevo != OrderStatus.CANCELLED) {
                return ResponseEntity.status(409).body(java.util.Map.of("message","Status change not allowed"));
            }
            orden.setStatus(nuevo);
        }
        if (req.fechaPedido != null) {
            orden.setFechaPedido(req.fechaPedido);
        }
        if (req.destinatario != null || req.direccion != null || req.region != null || req.ciudad != null || req.codigoPostal != null) {
            desarrollador.api.models.Direccion d = orden.getDireccion();
            if (d == null) {
                d = new desarrollador.api.models.Direccion();
                d.setUser(orden.getUser());
            }
            if (req.destinatario != null) d.setNombreDestinatario(req.destinatario);
            if (req.direccion != null) d.setDireccion(req.direccion);
            if (req.region != null) d.setRegion(req.region);
            if (req.ciudad != null) d.setCiudad(req.ciudad);
            if (req.codigoPostal != null) d.setCodigoPostal(req.codigoPostal);
            orden.setDireccion(d);
        }
        ordenRepositorio.save(orden);
        return ResponseEntity.ok(desarrollador.api.dto.OrdenDTO.fromEntity(orden, ordenServicio.listarItems(orden)));
    }
}
