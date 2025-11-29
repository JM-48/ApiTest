package desarrollador.api.controllers;

import desarrollador.api.models.Comuna;
import desarrollador.api.models.Region;
import desarrollador.api.repositories.ComunaRepositorio;
import desarrollador.api.repositories.RegionRepositorio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping
public class RegionComunaController {
    private final RegionRepositorio regionRepositorio;
    private final ComunaRepositorio comunaRepositorio;

    public RegionComunaController(RegionRepositorio regionRepositorio, ComunaRepositorio comunaRepositorio) {
        this.regionRepositorio = regionRepositorio;
        this.comunaRepositorio = comunaRepositorio;
    }

    @GetMapping("/regiones")
    public List<Region> regiones() {
        return regionRepositorio.findAll();
    }

    @GetMapping("/comunas/{regionNombre}")
    public List<Comuna> comunasPorRegion(@PathVariable String regionNombre) {
        return regionRepositorio.findByNombreIgnoreCase(regionNombre)
                .map(comunaRepositorio::findByRegion)
                .orElse(Collections.emptyList());
    }
}
