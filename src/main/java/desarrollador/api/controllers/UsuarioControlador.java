package desarrollador.api.controllers;

import desarrollador.api.dto.AuthDtos;
import desarrollador.api.models.Profile;
import desarrollador.api.models.Role;
import desarrollador.api.models.User;
import desarrollador.api.services.UsuarioServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioControlador {
    private final UsuarioServicio servicio;

    public UsuarioControlador(UsuarioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        User u = servicio.me(auth.getName());
        return ResponseEntity.ok(AuthDtos.UserDto.from(u));
    }

    @GetMapping("/me/datos-compra")
    public ResponseEntity<?> datosCompra(Authentication auth) {
        User u = servicio.me(auth.getName());
        Profile p = u.getProfile();
        java.util.List<String> missing = new java.util.ArrayList<>();
        if (p == null || p.getNombre() == null || p.getNombre().isBlank()) missing.add("nombre");
        if (p == null || p.getApellido() == null || p.getApellido().isBlank()) missing.add("apellido");
        if (p == null || p.getTelefono() == null || p.getTelefono().isBlank()) missing.add("telefono");
        if (p == null || p.getDireccion() == null || p.getDireccion().isBlank()) missing.add("direccion");
        if (p == null || p.getRegion() == null || p.getRegion().isBlank()) missing.add("region");
        if (p == null || p.getCiudad() == null || p.getCiudad().isBlank()) missing.add("ciudad");
        if (p == null || p.getCodigoPostal() == null || p.getCodigoPostal().isBlank()) missing.add("codigoPostal");
        return ResponseEntity.ok(java.util.Map.of(
                "user", AuthDtos.UserDto.from(u),
                "missing", missing
        ));
    }

    @PatchMapping("/me")
    public ResponseEntity<?> actualizar(Authentication auth, @RequestBody Profile profile) {
        User u = servicio.actualizarPerfil(auth.getName(), profile);
        return ResponseEntity.ok(AuthDtos.UserDto.from(u));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER_AD')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        User u = servicio.obtener(id);
        return ResponseEntity.ok(AuthDtos.UserDto.from(u));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER_AD')")
    public ResponseEntity<?> listar(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(required = false) String q) {
        Page<User> result = servicio.listar(page, size, q);
        Page<AuthDtos.UserDto> dto = new PageImpl<>(
                result.getContent().stream().map(AuthDtos.UserDto::from).toList(),
                PageRequest.of(page, size),
                result.getTotalElements()
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER_AD')")
    public ResponseEntity<?> crear(@RequestBody AuthDtos.RegisterRequest req) {
        Profile p = new Profile();
        p.setNombre(req.nombre);
        p.setApellido(req.apellido);
        p.setTelefono(req.telefono);
        p.setDireccion(req.direccion);
        p.setRegion(req.region);
        p.setCiudad(req.ciudad);
        p.setCodigoPostal(req.codigoPostal);
        Role role = req.role != null ? Role.valueOf(req.role.toUpperCase()) : Role.CLIENT;
        User u = servicio.crearPorAdmin(req.email, req.password, role, p);
        return ResponseEntity.status(201).body(AuthDtos.UserDto.from(u));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER_AD')")
    public ResponseEntity<?> actualizarPorAdmin(@PathVariable Long id, @RequestBody AuthDtos.RegisterRequest req) {
        Profile p = new Profile();
        p.setNombre(req.nombre);
        p.setApellido(req.apellido);
        p.setTelefono(req.telefono);
        p.setDireccion(req.direccion);
        p.setRegion(req.region);
        p.setCiudad(req.ciudad);
        p.setCodigoPostal(req.codigoPostal);
        Role role = req.role != null ? Role.valueOf(req.role.toUpperCase()) : null;
        User u = servicio.actualizarPorAdmin(id, req.email, role, p);
        return ResponseEntity.ok(AuthDtos.UserDto.from(u));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER_AD')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
