package desarrollador.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import desarrollador.api.models.Categoria;
import desarrollador.api.models.Comuna;
import desarrollador.api.models.Producto;
import desarrollador.api.models.Region;
import desarrollador.api.repositories.CategoriaRepositorio;
import desarrollador.api.repositories.ComunaRepositorio;
import desarrollador.api.repositories.ProductoRepositorio;
import desarrollador.api.repositories.RegionRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class InicializadorDatos {
    private final CategoriaRepositorio categoriaRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final RegionRepositorio regionRepositorio;
    private final ComunaRepositorio comunaRepositorio;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(InicializadorDatos.class);

    public InicializadorDatos(CategoriaRepositorio categoriaRepositorio,
                              ProductoRepositorio productoRepositorio,
                              RegionRepositorio regionRepositorio,
                              ComunaRepositorio comunaRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.regionRepositorio = regionRepositorio;
        this.comunaRepositorio = comunaRepositorio;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void cargar() {
        try {
            cargarCategoriasSiVacio();
            cargarRegionesSiVacio();
        } catch (Exception e) {
            log.warn("Fallo en carga inicial", e);
        }
    }

    private void cargarCategoriasSiVacio() throws IOException {
        if (categoriaRepositorio.count() > 0) return;
        JsonNode node = leerJson("categorias.json", "datos/categorias.json");
        if (node != null && node.isArray()) {
            List<Categoria> list = new ArrayList<>();
            for (JsonNode n : node) {
                String nombre = n.asText();
                if (nombre != null && !nombre.isBlank()) {
                    Categoria c = new Categoria();
                    c.setNombre(nombre);
                    list.add(c);
                }
            }
            categoriaRepositorio.saveAll(list);
            log.info("Categorias cargadas: {}", list.size());
        }
    }

    private void cargarRegionesSiVacio() throws IOException {
        if (regionRepositorio.count() > 0) return;
        JsonNode node = leerJson("ubicaciones.json", "datos/ubicaciones.json");
        if (node != null && node.isArray()) {
            int regiones = 0;
            int comunas = 0;
            for (JsonNode n : node) {
                String nombreRegion = n.get("region").asText();
                Region r = new Region();
                r.setNombre(nombreRegion);
                r = regionRepositorio.save(r);
                Iterator<JsonNode> it = n.get("comunas").elements();
                while (it.hasNext()) {
                    String nombreComuna = it.next().asText();
                    Comuna c = new Comuna();
                    c.setNombre(nombreComuna);
                    c.setRegion(r);
                    comunaRepositorio.save(c);
                    comunas++;
                }
                regiones++;
            }
            log.info("Regiones cargadas: {}, Comunas cargadas: {}", regiones, comunas);
        }
    }

    private void cargarProductosSiVacio() throws IOException {
        if (productoRepositorio.count() > 0) return;
        JsonNode node = leerJson("productos.json", "datos/productos.json");
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
            log.info("Productos cargados: {}", total);
        }
    }

    private JsonNode leerJson(String rootFileName, String classpathFile) throws IOException {
        Path rootPath = Path.of("..","" + rootFileName);
        if (Files.exists(rootPath)) {
            byte[] bytes = Files.readAllBytes(rootPath);
            return mapper.readTree(bytes);
        }
        try {
            return mapper.readTree(Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathFile));
        } catch (Exception e) {
            return null;
        }
    }

    private String textOf(JsonNode n, String field) {
        return n.has(field) && !n.get(field).isNull() ? n.get(field).asText() : null;
    }
}
