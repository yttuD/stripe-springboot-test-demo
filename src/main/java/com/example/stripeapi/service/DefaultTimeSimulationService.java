package com.example.stripeapi.service;

import com.stripe.exception.StripeException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Profile("!test")
public class DefaultTimeSimulationService implements TimeSimulationService {
    
    @Override
    public Optional<String> getTestClockId(String identifier) {
        // En producción no usamos TestClocks, retornamos vacío
        return Optional.empty();
    }

    @Override
    public String adaptPaymentMethodToken(String originalToken) {
        // En producción los tokens ya son reales, no necesitan aislarse
        return originalToken;
    }
}
