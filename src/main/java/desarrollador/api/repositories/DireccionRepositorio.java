package desarrollador.api.repositories;

import desarrollador.api.models.Direccion;
import desarrollador.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionRepositorio extends JpaRepository<Direccion, Long> {
    List<Direccion> findByUser(User user);
}

