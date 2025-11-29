package desarrollador.api.services;

import desarrollador.api.models.Categoria;
import desarrollador.api.models.Producto;
import desarrollador.api.repositories.CategoriaRepositorio;
import desarrollador.api.repositories.ProductoRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductoServicio {
    private final ProductoRepositorio productoRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;

    public ProductoServicio(ProductoRepositorio productoRepositorio, CategoriaRepositorio categoriaRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
    }

    public List<Producto> listar() {
        return productoRepositorio.findAll();
    }

    public Producto obtener(Long id) {
        return productoRepositorio.findById(id).orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
    }

    public Producto crear(Producto producto, Long categoriaId) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("Nombre requerido");
        }
        if (producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("Precio debe ser mayor a 0");
        }
        Categoria categoria = categoriaRepositorio.findById(categoriaId)
                .orElseThrow(() -> new NoSuchElementException("Categoria no encontrada"));
        producto.setCategoria(categoria);
        return productoRepositorio.save(producto);
    }

    public Producto actualizar(Long id, Producto cambios, Long categoriaId) {
        Producto existente = obtener(id);
        if (cambios.getNombre() != null && !cambios.getNombre().isBlank()) {
            existente.setNombre(cambios.getNombre());
        }
        if (cambios.getDescripcion() != null) {
            existente.setDescripcion(cambios.getDescripcion());
        }
        if (cambios.getPrecio() > 0) {
            existente.setPrecio(cambios.getPrecio());
        }
        if (cambios.getImagenUrl() != null) {
            existente.setImagenUrl(cambios.getImagenUrl());
        }
        if (cambios.getStock() != null) {
            existente.setStock(cambios.getStock());
        }
        if (categoriaId != null) {
            Categoria categoria = categoriaRepositorio.findById(categoriaId)
                    .orElseThrow(() -> new NoSuchElementException("Categoria no encontrada"));
            existente.setCategoria(categoria);
        }
        return productoRepositorio.save(existente);
    }

    public void eliminar(Long id) {
        if (!productoRepositorio.existsById(id)) {
            throw new NoSuchElementException("Producto no encontrado");
        }
        productoRepositorio.deleteById(id);
    }
}
