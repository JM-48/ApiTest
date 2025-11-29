package desarrollador.api.services;

import desarrollador.api.models.Categoria;
import desarrollador.api.repositories.CategoriaRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CategoriaServicio {
    private final CategoriaRepositorio repositorio;

    public CategoriaServicio(CategoriaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Categoria> listar() {
        return repositorio.findAll();
    }

    public Categoria obtener(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new NoSuchElementException("Categoria no encontrada"));
    }

    public Categoria crear(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new IllegalArgumentException("Nombre requerido");
        }
        if (repositorio.existsByNombreIgnoreCase(categoria.getNombre())) {
            throw new IllegalArgumentException("Categoria ya existe");
        }
        return repositorio.save(categoria);
    }

    public Categoria actualizar(Long id, Categoria cambios) {
        Categoria existente = obtener(id);
        String nombre = cambios.getNombre();
        if (nombre != null && !nombre.isBlank()) {
            if (!nombre.equalsIgnoreCase(existente.getNombre()) && repositorio.existsByNombreIgnoreCase(nombre)) {
                throw new IllegalArgumentException("Categoria ya existe");
            }
            existente.setNombre(nombre);
        }
        return repositorio.save(existente);
    }

    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            throw new NoSuchElementException("Categoria no encontrada");
        }
        repositorio.deleteById(id);
    }
}
