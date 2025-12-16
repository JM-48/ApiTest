package desarrollador.api.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "detalle_orden")
public class DetalleOrden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Orden orden;

    @ManyToOne(optional = false)
    private Producto producto;

    @Min(1)
    @Column(nullable = false)
    private int cantidad;

    @Positive
    @Column(nullable = false)
    private double precioUnitario;

    @Positive
    @Column(nullable = false)
    private double total;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Orden getOrden() { return orden; }
    public void setOrden(Orden orden) { this.orden = orden; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}

