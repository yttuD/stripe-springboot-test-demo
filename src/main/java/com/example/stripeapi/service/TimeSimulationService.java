package com.example.stripeapi.service;

import com.stripe.exception.StripeException;
import java.util.Optional;

public interface TimeSimulationService {
    Optional<String> getTestClockId(String identifier) throws StripeException;
    String adaptPaymentMethodToken(String originalToken);
}
