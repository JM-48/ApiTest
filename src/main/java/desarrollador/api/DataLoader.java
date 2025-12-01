package desarrollador.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import desarrollador.api.models.Producto;
import desarrollador.api.repositories.ProductoRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataLoader implements CommandLineRunner {
    private final ProductoRepositorio productoRepositorio;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(DataLoader.class);

    public DataLoader(ProductoRepositorio productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    @Override
    public void run(String... args) throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("productos.json");
        if (is == null) {
            log.info("Archivo productos.json no encontrado en resources");
            return;
        }
        JsonNode node = mapper.readTree(is);
        if (node == null || !node.isArray()) {
            return;
        }

        long count = productoRepositorio.count();
        if (count == 0) {
            int total = 0;
            for (JsonNode n : node) {
                String nombre = textOf(n, "nombre");
                String descripcion = textOf(n, "descripcion");
                double precio = n.has("precio") ? n.get("precio").asDouble() : 0;
                String tipo = textOf(n, "tipo");
                String imagen = textOf(n, "imagen");
                Integer stock = n.has("stock") ? n.get("stock").asInt() : null;
                if (nombre != null && tipo != null) {
                    Producto p = new Producto();
                    p.setNombre(nombre);
                    p.setDescripcion(descripcion);
                    p.setPrecio(precio);
                    p.setCategoriaNombre(tipo);
                    p.setImagenUrl(imagen);
                    p.setStock(stock);
                    productoRepositorio.save(p);
                    total++;
                }
            }
            log.info("Productos cargados correctamente: {}", total);
            return;
        }

        Map<String, String> tiposPorNombre = new HashMap<>();
        for (JsonNode n : node) {
            String nombre = textOf(n, "nombre");
            String tipo = textOf(n, "tipo");
            if (nombre != null && tipo != null) {
                tiposPorNombre.put(nombre.toLowerCase().trim(), tipo.trim());
            }
        }
        List<Producto> existentes = productoRepositorio.findAll();
        int actualizados = 0;
        for (Producto p : existentes) {
            if (p.getCategoriaNombre() == null && p.getNombre() != null) {
                String t = tiposPorNombre.get(p.getNombre().toLowerCase().trim());
                if (t != null) {
                    p.setCategoriaNombre(t);
                    productoRepositorio.save(p);
                    actualizados++;
                }
            }
        }
        if (actualizados > 0) {
            log.info("Productos actualizados categoria desde JSON: {}", actualizados);
        }
    }

    private String textOf(JsonNode n, String field) {
        return n.has(field) && !n.get(field).isNull() ? n.get(field).asText() : null;
    }
}
