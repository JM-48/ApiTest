package desarrollador.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import desarrollador.api.models.Compra;

@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class CompraDTO {
    private Long id;
    private Long ordenId;
    private String estado;
    private double monto;
    private String referenciaPago;
    private java.time.Instant fechaPago;

    public static CompraDTO fromEntity(Compra c) {
        CompraDTO dto = new CompraDTO();
        dto.id = c.getId();
        dto.ordenId = c.getOrden() == null ? null : c.getOrden().getId();
        dto.estado = c.getEstado() == null ? null : c.getEstado().name();
        dto.monto = c.getMonto();
        dto.referenciaPago = c.getReferenciaPago();
        dto.fechaPago = c.getFechaPago();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrdenId() { return ordenId; }
    public void setOrdenId(Long ordenId) { this.ordenId = ordenId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
    public java.time.Instant getFechaPago() { return fechaPago; }
    public void setFechaPago(java.time.Instant fechaPago) { this.fechaPago = fechaPago; }
}

