package com.example.stripeapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull(message = "El monto no puede ser nulo")
    @Min(value = 50, message = "El monto mínimo procesable es 50 centavos")
    private Long amount; // Amount in cents

    @NotBlank(message = "La moneda es obligatoria")
    private String currency;

    @NotBlank(message = "El userId es obligatorio")
    private String userId;

    @NotBlank(message = "El ID del método de pago es obligatorio")
    private String paymentMethodId;

    // Genera un UUID único por petición si el cliente frontend no lo envía.
    // Garantiza que los reintentos automáticos usen siempre la misma clave.
    private String idempotencyKey = java.util.UUID.randomUUID().toString();

    private Boolean isSubscription = false; // Flag para determinar el tipo de pago
}
