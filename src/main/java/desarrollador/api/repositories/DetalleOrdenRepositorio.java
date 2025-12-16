package desarrollador.api.repositories;

import desarrollador.api.models.DetalleOrden;
import desarrollador.api.models.Orden;
import desarrollador.api.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DetalleOrdenRepositorio extends JpaRepository<DetalleOrden, Long> {
    List<DetalleOrden> findByOrden(Orden orden);
    Optional<DetalleOrden> findByOrdenAndProducto(Orden orden, Producto producto);
}

