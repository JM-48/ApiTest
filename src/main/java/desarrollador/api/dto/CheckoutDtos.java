package desarrollador.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Min;
import java.util.List;

public class CheckoutDtos {
    public static class CheckoutItem {
        public String productoId;
        @NotBlank
        public String nombre;
        @PositiveOrZero
        public double precioUnitario;
        @Min(1)
        public int cantidad;
    }

    public static class CheckoutRequest {
        @NotNull
        public List<CheckoutItem> items;
        @PositiveOrZero
        public double total;
        @NotBlank
        public String metodoEnvio;
        @NotBlank
        public String metodoPago;
        @NotBlank
        public String destinatario;
        @NotBlank
        public String direccion;
        @NotBlank
        public String region;
        @NotBlank
        public String ciudad;
        @NotBlank
        public String codigoPostal;
    }

    public static class ConfirmRequest {
        @NotBlank
        public String referenciaPago;
    }
}

