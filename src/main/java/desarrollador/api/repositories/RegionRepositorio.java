package desarrollador.api.repositories;

import desarrollador.api.models.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepositorio extends JpaRepository<Region, String> {
    Optional<Region> findByNombreIgnoreCase(String nombre);
}
