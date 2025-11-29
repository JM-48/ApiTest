package desarrollador.api.controllers;

import desarrollador.api.dto.AuthDtos;
import desarrollador.api.models.Profile;
import desarrollador.api.models.Role;
import desarrollador.api.models.User;
import desarrollador.api.security.JwtServicio;
import desarrollador.api.services.UsuarioServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthControlador {
    private final UsuarioServicio servicio;
    private final JwtServicio jwt;

    public AuthControlador(UsuarioServicio servicio, JwtServicio jwt) {
        this.servicio = servicio;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDtos.RegisterRequest req) {
        Profile p = new Profile();
        p.setNombre(req.nombre);
        p.setApellido(req.apellido);
        p.setTelefono(req.telefono);
        p.setDireccion(req.direccion);
        p.setCiudad(req.ciudad);
        p.setCodigoPostal(req.codigoPostal);
        Role role = req.role != null ? Role.valueOf(req.role.toUpperCase()) : Role.USER;
        User u = servicio.registrar(req.email, req.password, role, p);
        return ResponseEntity.status(201).body(AuthDtos.UserDto.from(u));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDtos.LoginRequest req) {
        User u = servicio.login(req.email, req.password);
        String token = jwt.generar(u, 1000L * 60 * 60 * 8);
        AuthDtos.LoginResponse res = new AuthDtos.LoginResponse();
        res.token = token;
        res.user = AuthDtos.UserDto.from(u);
        return ResponseEntity.ok(res);
    }
}
