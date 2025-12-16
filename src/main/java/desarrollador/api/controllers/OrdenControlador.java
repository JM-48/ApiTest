package desarrollador.api.controllers;

import desarrollador.api.models.Orden;
import desarrollador.api.models.User;
import desarrollador.api.repositories.OrdenRepositorio;
import desarrollador.api.services.OrdenServicio;
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las órdenes (admin)")
    public ResponseEntity<?> todas() {
        List<Orden> list = ordenRepositorio.findAll();
        return ResponseEntity.ok(list.stream()
                .map(o -> desarrollador.api.dto.OrdenDTO.fromEntity(o, ordenServicio.listarItems(o)))
                .toList());
    }
}
