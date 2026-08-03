package com.example.stripeapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClockRequest {
    @NotBlank(message = "El ID del reloj (TestClock) es obligatorio")
    private String testClockId;
}
