package desarrollador.api.controllers;

import desarrollador.api.models.Comuna;
import desarrollador.api.models.Region;
import desarrollador.api.services.UbicacionServicio;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ubicaciones")
public class UbicacionControlador {
    private final UbicacionServicio servicio;

    public UbicacionControlador(UbicacionServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/regiones")
    public List<Region> regiones() {
        return servicio.regiones();
    }

    @GetMapping("/comunas/{regionNombre}")
    public List<Comuna> comunas(@PathVariable String regionNombre) {
        return servicio.comunasPorRegion(regionNombre);
    }
}
