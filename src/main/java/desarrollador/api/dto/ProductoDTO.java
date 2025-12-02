package desarrollador.api.dto;

import desarrollador.api.models.Producto;

public class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String categoriaNombre;
    private double precio;
    private String imagenUrl;
    private Integer stock;

    public ProductoDTO() {
    }

    public ProductoDTO(Long id, String nombre, String descripcion, String categoriaNombre, double precio, String imagenUrl, Integer stock) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoriaNombre = categoriaNombre;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.stock = stock;
    }

    public static ProductoDTO fromEntity(Producto p) {
        return new ProductoDTO(p.getId(), p.getNombre(), p.getDescripcion(), p.getCategoriaNombre(), p.getPrecio(), p.getImagenUrl(), p.getStock());
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

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
