package desarrollador.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import desarrollador.api.models.Producto;

@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private double precio;
    private Double precioOriginal;
    private String imagen;
    private Integer stock;

    public ProductoDTO() {
    }

    public ProductoDTO(Long id, String nombre, String descripcion, String tipo, double precio, Double precioOriginal, String imagen, Integer stock) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.precio = precio;
        this.precioOriginal = precioOriginal;
        this.imagen = imagen;
        this.stock = stock;
    }

    public static ProductoDTO fromEntity(Producto p) {
        Double original = p.getPrecio() > 0 ? Math.round((p.getPrecio() / 1.19) * 100.0) / 100.0 : null;
        return new ProductoDTO(p.getId(), p.getNombre(), p.getDescripcion(), p.getCategoriaNombre(), p.getPrecio(), original, p.getImagenUrl(), p.getStock());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Double getPrecioOriginal() {
        return precioOriginal;
    }

    public void setPrecioOriginal(Double precioOriginal) {
        this.precioOriginal = precioOriginal;
    }
}
