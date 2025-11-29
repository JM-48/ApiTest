package desarrollador.api.dto;

import desarrollador.api.models.Producto;

public class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private double precio;
    private String imagen;
    private Integer stock;

    public ProductoDTO() {
    }

    public ProductoDTO(Long id, String nombre, String descripcion, String tipo, double precio, String imagen, Integer stock) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.precio = precio;
        this.imagen = imagen;
        this.stock = stock;
    }

    public static ProductoDTO fromEntity(Producto p) {
        String t = p.getCategoria() != null ? p.getCategoria().getNombre() : null;
        return new ProductoDTO(p.getId(), p.getNombre(), p.getDescripcion(), t, p.getPrecio(), p.getImagenUrl(), p.getStock());
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
}

