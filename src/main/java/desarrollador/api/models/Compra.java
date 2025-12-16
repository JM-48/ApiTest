package desarrollador.api.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

@Entity
@Table(name = "compra")
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Orden orden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseStatus estado = PurchaseStatus.PENDIENTE;

    private String referenciaPago;

    @Positive
    @Column(nullable = false)
    private double monto;

    private Instant fechaPago;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Orden getOrden() { return orden; }
    public void setOrden(Orden orden) { this.orden = orden; }
    public PurchaseStatus getEstado() { return estado; }
    public void setEstado(PurchaseStatus estado) { this.estado = estado; }
    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public Instant getFechaPago() { return fechaPago; }
    public void setFechaPago(Instant fechaPago) { this.fechaPago = fechaPago; }
}

