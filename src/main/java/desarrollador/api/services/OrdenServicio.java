package desarrollador.api.services;

import desarrollador.api.models.*;
import desarrollador.api.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrdenServicio {
    private final OrdenRepositorio ordenRepositorio;
    private final DetalleOrdenRepositorio detalleRepositorio;
    private final DireccionRepositorio direccionRepositorio;
    private final CompraRepositorio compraRepositorio;
    private final UserRepositorio userRepositorio;
    private final ProductoRepositorio productoRepositorio;

    public OrdenServicio(OrdenRepositorio ordenRepositorio,
                         DetalleOrdenRepositorio detalleRepositorio,
                         DireccionRepositorio direccionRepositorio,
                         CompraRepositorio compraRepositorio,
                         UserRepositorio userRepositorio,
                         ProductoRepositorio productoRepositorio) {
        this.ordenRepositorio = ordenRepositorio;
        this.detalleRepositorio = detalleRepositorio;
        this.direccionRepositorio = direccionRepositorio;
        this.compraRepositorio = compraRepositorio;
        this.userRepositorio = userRepositorio;
        this.productoRepositorio = productoRepositorio;
    }

    public User getUserByEmail(String email) {
        return userRepositorio.findByEmailIgnoreCase(email).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    public Orden getOrCreateCart(User user, Direccion direccion) {
        return ordenRepositorio.findByUserAndStatus(user, OrderStatus.CART).orElseGet(() -> {
            Orden o = new Orden();
            o.setUser(user);
            o.setDireccion(direccion);
            o.setStatus(OrderStatus.CART);
            o.setTotal(0.0);
            return ordenRepositorio.save(o);
        });
    }

    public List<DetalleOrden> listarItems(Orden orden) {
        return detalleRepositorio.findByOrden(orden);
    }

    @Transactional
    public Orden agregarItem(Orden orden, Long productoId, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");
        Producto p = productoRepositorio.findById(productoId).orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
        DetalleOrden item = detalleRepositorio.findByOrdenAndProducto(orden, p).orElse(null);
        if (item == null) {
            item = new DetalleOrden();
            item.setOrden(orden);
            item.setProducto(p);
            item.setCantidad(cantidad);
            item.setPrecioUnitario(p.getPrecio());
            item.setTotal(p.getPrecio() * cantidad);
        } else {
            item.setCantidad(item.getCantidad() + cantidad);
            item.setTotal(item.getPrecioUnitario() * item.getCantidad());
        }
        detalleRepositorio.save(item);
        recalcularTotal(orden);
        return ordenRepositorio.save(orden);
    }

    @Transactional
    public Orden actualizarItem(Orden orden, Long productoId, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");
        Producto p = productoRepositorio.findById(productoId).orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
        DetalleOrden item = detalleRepositorio.findByOrdenAndProducto(orden, p).orElseThrow(() -> new NoSuchElementException("Item no encontrado"));
        item.setCantidad(cantidad);
        item.setTotal(item.getPrecioUnitario() * cantidad);
        detalleRepositorio.save(item);
        recalcularTotal(orden);
        return ordenRepositorio.save(orden);
    }

    @Transactional
    public Orden eliminarItem(Orden orden, Long productoId) {
        Producto p = productoRepositorio.findById(productoId).orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
        DetalleOrden item = detalleRepositorio.findByOrdenAndProducto(orden, p).orElseThrow(() -> new NoSuchElementException("Item no encontrado"));
        detalleRepositorio.delete(item);
        recalcularTotal(orden);
        return ordenRepositorio.save(orden);
    }

    private void recalcularTotal(Orden orden) {
        double total = detalleRepositorio.findByOrden(orden).stream().mapToDouble(DetalleOrden::getTotal).sum();
        orden.setTotal(total);
    }

    public Direccion crearDireccion(User user, Direccion d) {
        d.setUser(user);
        return direccionRepositorio.save(d);
    }

    @Transactional
    public Orden checkout(Orden orden) {
        if (orden.getItems() == null || detalleRepositorio.findByOrden(orden).isEmpty()) {
            throw new IllegalArgumentException("Carrito vacío");
        }
        if (orden.getFechaPedido() == null) {
            orden.setFechaPedido(java.time.Instant.now());
        }
        orden.setStatus(OrderStatus.PENDING);
        return ordenRepositorio.save(orden);
    }

    @Transactional
    public Compra confirmarCompra(Orden orden, String referenciaPago) {
        if (orden.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Orden no pendiente");
        }
        Compra compra = compraRepositorio.findByOrden(orden).orElse(new Compra());
        compra.setOrden(orden);
        compra.setEstado(PurchaseStatus.CONFIRMADA);
        compra.setReferenciaPago(referenciaPago);
        compra.setMonto(orden.getTotal());
        compra.setFechaPago(java.time.Instant.now());
        compraRepositorio.save(compra);
        orden.setStatus(OrderStatus.PAID);
        ordenRepositorio.save(orden);
        return compra;
    }

    public List<Orden> listarPorUsuario(User user) {
        return ordenRepositorio.findByUser(user);
    }

    public Orden obtener(Long id) {
        return ordenRepositorio.findById(id).orElseThrow(() -> new NoSuchElementException("Orden no encontrada"));
    }

    public Producto resolverProducto(String productoId, String nombre) {
        Producto byId = null;
        if (productoId != null && !productoId.isBlank()) {
            try {
                Long pid = Long.valueOf(productoId);
                byId = productoRepositorio.findById(pid).orElse(null);
            } catch (NumberFormatException ignored) {
            }
        }
        if (byId != null) return byId;
        if (nombre != null && !nombre.isBlank()) {
            return productoRepositorio.findByNombreIgnoreCase(nombre).orElse(null);
        }
        return null;
    }

    public Orden crearOrden(User user, Direccion d, double total) {
        Orden o = new Orden();
        o.setUser(user);
        o.setDireccion(d);
        o.setStatus(OrderStatus.PENDING);
        o.setTotal(total);
        o.setFechaPedido(java.time.Instant.now());
        return ordenRepositorio.save(o);
    }

    public DetalleOrden agregarItemConPrecio(Orden orden, Producto p, int cantidad, double precioUnitario) {
        DetalleOrden item = detalleRepositorio.findByOrdenAndProducto(orden, p).orElse(null);
        if (item == null) {
            item = new DetalleOrden();
            item.setOrden(orden);
            item.setProducto(p);
            item.setCantidad(cantidad);
            item.setPrecioUnitario(precioUnitario);
            item.setTotal(precioUnitario * cantidad);
        } else {
            item.setCantidad(item.getCantidad() + cantidad);
            item.setPrecioUnitario(precioUnitario);
            item.setTotal(item.getPrecioUnitario() * item.getCantidad());
        }
        return detalleRepositorio.save(item);
    }
}
