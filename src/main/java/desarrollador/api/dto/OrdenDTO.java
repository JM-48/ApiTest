package desarrollador.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import desarrollador.api.models.DetalleOrden;
import desarrollador.api.models.Direccion;
import desarrollador.api.models.Orden;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class OrdenDTO {
    private Long id;
    private String status;
    private double total;
    private java.time.Instant fechaPedido;
    private String destinatario;
    private String direccion;
    private String region;
    private String ciudad;
    private String codigoPostal;
    private List<DetalleOrdenDTO> items;

    public static OrdenDTO fromEntity(Orden o, List<DetalleOrden> detalles) {
        OrdenDTO dto = new OrdenDTO();
        dto.id = o.getId();
        dto.status = o.getStatus().name();
        dto.total = o.getTotal();
        dto.fechaPedido = o.getFechaPedido();
        Direccion d = o.getDireccion();
        if (d != null) {
            dto.destinatario = d.getNombreDestinatario();
            dto.direccion = d.getDireccion();
            dto.region = d.getRegion();
            dto.ciudad = d.getCiudad();
            dto.codigoPostal = d.getCodigoPostal();
        }
        dto.items = detalles == null ? null : detalles.stream().map(DetalleOrdenDTO::fromEntity).collect(Collectors.toList());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public java.time.Instant getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(java.time.Instant fechaPedido) { this.fechaPedido = fechaPedido; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
    public List<DetalleOrdenDTO> getItems() { return items; }
    public void setItems(List<DetalleOrdenDTO> items) { this.items = items; }
}
