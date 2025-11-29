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

    @PatchMapping("/me")
    public ResponseEntity<?> actualizar(Authentication auth, @RequestBody Profile profile) {
        User u = servicio.actualizarPerfil(auth.getName(), profile);
        return ResponseEntity.ok(AuthDtos.UserDto.from(u));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        User u = servicio.obtener(id);
        return ResponseEntity.ok(AuthDtos.UserDto.from(u));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@RequestBody AuthDtos.RegisterRequest req) {
        Profile p = new Profile();
        p.setNombre(req.nombre);
        p.setApellido(req.apellido);
        p.setTelefono(req.telefono);
        p.setDireccion(req.direccion);
        p.setCiudad(req.ciudad);
        p.setCodigoPostal(req.codigoPostal);
        Role role = req.role != null ? Role.valueOf(req.role.toUpperCase()) : Role.USER;
        User u = servicio.crearPorAdmin(req.email, req.password, role, p);
        return ResponseEntity.status(201).body(AuthDtos.UserDto.from(u));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarPorAdmin(@PathVariable Long id, @RequestBody AuthDtos.RegisterRequest req) {
        Profile p = new Profile();
        p.setNombre(req.nombre);
        p.setApellido(req.apellido);
        p.setTelefono(req.telefono);
        p.setDireccion(req.direccion);
        p.setCiudad(req.ciudad);
        p.setCodigoPostal(req.codigoPostal);
        Role role = req.role != null ? Role.valueOf(req.role.toUpperCase()) : null;
        User u = servicio.actualizarPorAdmin(id, req.email, role, p);
        return ResponseEntity.ok(AuthDtos.UserDto.from(u));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
