package com.example.stripeapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPaymentMethod {
    private String id; // ID interno de tu BD simulada (ej. "pm_1")
    private String stripeToken; // El token de prueba de Stripe (ej. "pm_card_visa")
    private String description; // Ej: "Visa terminada en 4242"
    private boolean isWorking; // Solo para mostrar en el frontend si es el método bueno o malo
    private boolean isDefault; // Indica si es la tarjeta preferida del usuario
}
