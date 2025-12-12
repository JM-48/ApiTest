package desarrollador.api.services;

import desarrollador.api.models.Producto;
import desarrollador.api.repositories.ProductoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServicioTests {
    @Mock
    ProductoRepositorio repo;

    @InjectMocks
    ProductoServicio servicio;

    private Producto nuevo() {
        Producto p = new Producto();
        p.setNombre("Prod");
        p.setPrecio(10);
        p.setCategoriaNombre("Tipo");
        p.setStock(5);
        return p;
    }

    @Test
    void crear_normaliza_stock_negativo_a_cero() {
        Producto p = nuevo();
        p.setStock(-5);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Producto res = servicio.crear(p);
        assertEquals(0, res.getStock());
    }

    @Test
    void crear_requiere_nombre_precio_y_tipo() {
        Producto p1 = nuevo();
        p1.setNombre(" ");
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(p1));
        Producto p2 = nuevo();
        p2.setPrecio(0);
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(p2));
        Producto p3 = nuevo();
        p3.setCategoriaNombre(null);
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(p3));
    }
}
