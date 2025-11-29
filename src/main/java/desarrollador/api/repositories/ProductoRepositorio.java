package desarrollador.api.repositories;

import desarrollador.api.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {
    Optional<Producto> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
