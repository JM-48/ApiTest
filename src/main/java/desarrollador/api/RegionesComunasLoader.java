package desarrollador.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import desarrollador.api.models.Comuna;
import desarrollador.api.models.Region;
import desarrollador.api.repositories.ComunaRepositorio;
import desarrollador.api.repositories.RegionRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class RegionesComunasLoader implements CommandLineRunner {
    private final RegionRepositorio regionRepositorio;
    private final ComunaRepositorio comunaRepositorio;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(RegionesComunasLoader.class);

    public RegionesComunasLoader(RegionRepositorio regionRepositorio, ComunaRepositorio comunaRepositorio) {
        this.regionRepositorio = regionRepositorio;
        this.comunaRepositorio = comunaRepositorio;
    }

    @Override
    public void run(String... args) throws Exception {
        if (regionRepositorio.count() > 0 || comunaRepositorio.count() > 0) {
            return;
        }
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("regiones_comunas.json");
        if (is == null) {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("datos/ubicaciones.json");
        }
        if (is == null) {
            return;
        }
        JsonNode node = mapper.readTree(is);
        if (node != null && node.isArray()) {
            for (JsonNode n : node) {
                String nombreRegion = n.get("region").asText();
                Region r = new Region(nombreRegion);
                r = regionRepositorio.save(r);
                for (JsonNode cnode : n.get("comunas")) {
                    String nombreComuna = cnode.asText();
                    Comuna c = new Comuna(nombreComuna, r);
                    comunaRepositorio.save(c);
                }
            }
            log.info("Regiones y comunas cargadas correctamente!");
        }
    }
}
