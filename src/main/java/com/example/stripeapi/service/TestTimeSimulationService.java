package com.example.stripeapi.service;

import com.stripe.exception.StripeException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Profile("test")
public class TestTimeSimulationService implements TimeSimulationService {

    @Override
    public Optional<String> getTestClockId(String identifier) throws StripeException {
        com.stripe.param.testhelpers.TestClockCreateParams clockParams = com.stripe.param.testhelpers.TestClockCreateParams.builder()
            .setFrozenTime(System.currentTimeMillis() / 1000L)
            .setName("TestClock_" + identifier.replace(" ", ""))
            .build();
        com.stripe.model.testhelpers.TestClock testClock = com.stripe.model.testhelpers.TestClock.create(clockParams);
        return Optional.of(testClock.getId());
    }

    @Override
    public String adaptPaymentMethodToken(String originalToken) {
        // [ARQUITECTURA - IMPORTANTE]: Stripe aísla estrictamente los objetos de prueba. 
        // Los tokens globales como 'pm_card_visa' no pertenecen a ninguna "dimensión de tiempo" (TestClock).
        // Convertimos a 'tok_' para poder instanciar un PaymentMethod aislado después.
        return originalToken.replace("pm_card_", "tok_");
    }
}
