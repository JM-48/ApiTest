package desarrollador.api.repositories;

import desarrollador.api.models.Compra;
import desarrollador.api.models.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompraRepositorio extends JpaRepository<Compra, Long> {
    Optional<Compra> findByOrden(Orden orden);
}

