package desarrollador.api.repositories;

import desarrollador.api.models.OrderStatus;
import desarrollador.api.models.Orden;
import desarrollador.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdenRepositorio extends JpaRepository<Orden, Long> {
    Optional<Orden> findByUserAndStatus(User user, OrderStatus status);
    List<Orden> findByUser(User user);
}

