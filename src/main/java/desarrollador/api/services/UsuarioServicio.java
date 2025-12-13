package desarrollador.api.services;

import desarrollador.api.models.Profile;
import desarrollador.api.models.Role;
import desarrollador.api.models.User;
import desarrollador.api.repositories.UserRepositorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.List;

@Service
public class UsuarioServicio {
    private final UserRepositorio repositorio;
    private final PasswordEncoder encoder;

    public UsuarioServicio(UserRepositorio repositorio, PasswordEncoder encoder) {
        this.repositorio = repositorio;
        this.encoder = encoder;
    }

    public User registrar(String email, String password, Role role, Profile profile) {
        if (repositorio.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email duplicado");
        }
        if (password == null || password.length() < 6 || password.equals(password.toLowerCase()) || password.chars().noneMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Password inválida");
        }
        User u = new User();
        u.setEmail(email.toLowerCase());
        u.setPasswordHash(encoder.encode(password));
        u.setRole(role == null ? Role.CLIENT : role);
        u.setProfile(profile);
        return repositorio.save(u);
    }

    public User login(String email, String password) {
        User u = repositorio.findByEmailIgnoreCase(email).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        if (!encoder.matches(password, u.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        return u;
    }

    public User me(String email) {
        return repositorio.findByEmailIgnoreCase(email).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    public User actualizarPerfil(String email, Profile profile) {
        User u = me(email);
        u.setProfile(profile);
        return repositorio.save(u);
    }

    public User obtener(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    public Page<User> listar(int page, int size, String q) {
        Pageable pageable = PageRequest.of(page, Math.max(1, Math.min(size, 100)));
        if (q != null && !q.isBlank()) {
            return repositorio.findByEmailContainingIgnoreCase(q, pageable);
        }
        return repositorio.findAll(pageable);
    }

    public User crearPorAdmin(String email, String password, Role role, Profile profile) {
        return registrar(email, password, role, profile);
    }

    public User actualizarPorAdmin(Long id, String email, Role role, Profile profile) {
        User u = obtener(id);
        if (email != null && !email.isBlank()) {
            String lower = email.toLowerCase();
            if (!lower.equalsIgnoreCase(u.getEmail()) && repositorio.existsByEmailIgnoreCase(lower)) {
                throw new IllegalArgumentException("Email duplicado");
            }
            u.setEmail(lower);
        }
        if (role != null) {
            u.setRole(role);
        }
        if (profile != null) {
            u.setProfile(profile);
        }
        return repositorio.save(u);
    }

    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new NoSuchElementException("Usuario no encontrado");
        }
        repositorio.deleteById(id);
    }
}
