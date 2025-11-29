package desarrollador.api.repositories;

import desarrollador.api.models.Comuna;
import desarrollador.api.models.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComunaRepositorio extends JpaRepository<Comuna, Long> {
    List<Comuna> findByRegion(Region region);
    List<Comuna> findByRegionNombreIgnoreCase(String regionNombre);
}
