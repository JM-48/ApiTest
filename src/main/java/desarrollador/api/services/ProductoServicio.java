package desarrollador.api.services;

import desarrollador.api.models.Producto;
import desarrollador.api.repositories.ProductoRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductoServicio {
    private final ProductoRepositorio productoRepositorio;

    public ProductoServicio(ProductoRepositorio productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    public List<Producto> listar() {
        return productoRepositorio.findAll();
    }

    public Producto obtener(Long id) {
        return productoRepositorio.findById(id).orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
    }

    public Producto crear(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("Nombre requerido");
        }
        if (producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("Precio debe ser mayor a 0");
        }
        if (producto.getCategoriaNombre() == null || producto.getCategoriaNombre().isBlank()) {
            throw new IllegalArgumentException("Tipo requerido");
        }
        if (producto.getStock() != null && producto.getStock() < 0) {
            producto.setStock(0);
        }
        return productoRepositorio.save(producto);
    }

    public Producto actualizar(Long id, Producto cambios) {
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
            if (cambios.getStock() < 0) {
                existente.setStock(0);
            } else {
                existente.setStock(cambios.getStock());
            }
        }
        if (cambios.getCategoriaNombre() != null) {
            if (cambios.getCategoriaNombre().isBlank()) {
                throw new IllegalArgumentException("Tipo requerido");
            }
            existente.setCategoriaNombre(cambios.getCategoriaNombre());
        }
        return productoRepositorio.save(existente);
    }

    public Producto actualizarImagen(Long id, String imagenUrl) {
        Producto p = obtener(id);
        p.setImagenUrl(imagenUrl);
        return productoRepositorio.save(p);
    }

    public void eliminar(Long id) {
        if (!productoRepositorio.existsById(id)) {
            throw new NoSuchElementException("Producto no encontrado");
        }
        productoRepositorio.deleteById(id);
    }
}
