package desarrollador.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import desarrollador.api.models.Categoria;
import desarrollador.api.models.Producto;
import desarrollador.api.repositories.CategoriaRepositorio;
import desarrollador.api.repositories.ProductoRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class DataLoader implements CommandLineRunner {
    private final ProductoRepositorio productoRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(DataLoader.class);

    public DataLoader(ProductoRepositorio productoRepositorio, CategoriaRepositorio categoriaRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productoRepositorio.count() > 0) {
            log.info("Tabla Producto con datos, no se cargan productos");
            return;
        }
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("productos.json");
        if (is == null) {
            log.info("Archivo productos.json no encontrado en resources");
            return;
        }
        JsonNode node = mapper.readTree(is);
        if (node != null && node.isArray()) {
            int total = 0;
            for (JsonNode n : node) {
                String nombre = textOf(n, "nombre");
                String descripcion = textOf(n, "descripcion");
                double precio = n.has("precio") ? n.get("precio").asDouble() : 0;
                String tipo = textOf(n, "tipo");
                String imagen = textOf(n, "imagen");
                Integer stock = n.has("stock") ? n.get("stock").asInt() : null;
                if (nombre != null && tipo != null) {
                    Categoria categoria = categoriaRepositorio.findByNombreIgnoreCase(tipo)
                            .orElseGet(() -> {
                                Categoria c = new Categoria();
                                c.setNombre(tipo);
                                return categoriaRepositorio.save(c);
                            });
                    Producto p = new Producto();
                    p.setNombre(nombre);
                    p.setDescripcion(descripcion);
                    p.setPrecio(precio);
                    p.setCategoria(categoria);
                    p.setImagenUrl(imagen);
                    p.setStock(stock);
                    productoRepositorio.save(p);
                    total++;
                }
            }
            log.info("Productos cargados correctamente: {}", total);
        }
    }

    private String textOf(JsonNode n, String field) {
        return n.has(field) && !n.get(field).isNull() ? n.get(field).asText() : null;
    }
}
