package desarrollador.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import desarrollador.api.models.DetalleOrden;
import desarrollador.api.models.Producto;

@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class DetalleOrdenDTO {
    private Long productoId;
    private String nombre;
    private double precioUnitario;
    private int cantidad;
    private double total;
    private String imagen;

    public static DetalleOrdenDTO fromEntity(DetalleOrden d) {
        Producto p = d.getProducto();
        DetalleOrdenDTO dto = new DetalleOrdenDTO();
        dto.productoId = p.getId();
        dto.nombre = p.getNombre();
        dto.precioUnitario = d.getPrecioUnitario();
        dto.cantidad = d.getCantidad();
        dto.total = d.getTotal();
        dto.imagen = p.getImagenUrl();
        return dto;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}

