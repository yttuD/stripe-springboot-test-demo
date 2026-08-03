package com.example.stripeapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private String status;
    private String fallbackLog; // Para enviarle al frontend el log del reintento
    private String testClockId; // Para avanzar el tiempo de la suscripción
}
