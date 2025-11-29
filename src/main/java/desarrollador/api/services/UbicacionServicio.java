package desarrollador.api.services;

import desarrollador.api.models.Comuna;
import desarrollador.api.models.Region;
import desarrollador.api.repositories.ComunaRepositorio;
import desarrollador.api.repositories.RegionRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UbicacionServicio {
    private final RegionRepositorio regionRepositorio;
    private final ComunaRepositorio comunaRepositorio;

    public UbicacionServicio(RegionRepositorio regionRepositorio, ComunaRepositorio comunaRepositorio) {
        this.regionRepositorio = regionRepositorio;
        this.comunaRepositorio = comunaRepositorio;
    }

    public List<Region> regiones() {
        return regionRepositorio.findAll();
    }

    public List<Comuna> comunasPorRegion(String regionNombre) {
        Region region = regionRepositorio.findByNombreIgnoreCase(regionNombre)
                .orElseThrow(() -> new NoSuchElementException("Region no encontrada"));
        return comunaRepositorio.findByRegion(region);
    }
}
