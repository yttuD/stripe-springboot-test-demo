package com.example.stripeapi.service;

import com.example.stripeapi.dto.PaymentRequest;
import com.example.stripeapi.dto.PaymentResponse;
import com.example.stripeapi.model.User;
import com.example.stripeapi.model.UserPaymentMethod;
import com.example.stripeapi.repository.MockUserRepository;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.CardException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService {

    private final MockUserRepository userRepository;
    private final StripeIntegrationService stripeIntegrationService;

    public PaymentService(MockUserRepository userRepository, StripeIntegrationService stripeIntegrationService) {
        this.userRepository = userRepository;
        this.stripeIntegrationService = stripeIntegrationService;
    }

    @Retryable(
        retryFor = {ApiConnectionException.class, RateLimitException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public PaymentResponse createPaymentIntent(PaymentRequest request) throws StripeException {
        User user = getUserOrThrow(request.getUserId());
        UserPaymentMethod method = getPaymentMethodOrThrow(user, request.getPaymentMethodId());

        try {
            return executePayment(request, user, method, null);
        } catch (CardException e) {
            log.warn("Fallo el método principal ({}). Buscando métodos de respaldo...", e.getStripeError().getDeclineCode());
            
            List<UserPaymentMethod> backupMethods = user.getPaymentMethods().stream()
                    .filter(pm -> !pm.getId().equals(request.getPaymentMethodId()))
                    .sorted(Comparator.comparing(UserPaymentMethod::isDefault).reversed())
                    .collect(Collectors.toList());

            if (backupMethods.isEmpty()) {
                throw e; 
            }

            CardException lastException = e;
            
            for (UserPaymentMethod backupMethod : backupMethods) {
                String fallbackMsg = "Un método falló. Saltando automáticamente al método de respaldo: " + backupMethod.getDescription();
                log.info(fallbackMsg);
                
                try {
                    return executePayment(request, user, backupMethod, fallbackMsg);
                } catch (CardException backupException) {
                    log.warn("Fallo el método de respaldo ({}). Intentando siguiente...", backupException.getStripeError().getDeclineCode());
                    lastException = backupException;
                }
            }
            
            throw lastException;
        }
    }

    private User getUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private UserPaymentMethod getPaymentMethodOrThrow(User user, String paymentMethodId) {
        return user.getPaymentMethods().stream()
                .filter(pm -> pm.getId().equals(paymentMethodId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado"));
    }

    private PaymentResponse executePayment(PaymentRequest request, User user, UserPaymentMethod method, String fallbackMsg) throws StripeException {
        if (Boolean.TRUE.equals(request.getIsSubscription())) {
            return stripeIntegrationService.createStripeSubscription(request, user, method, fallbackMsg);
        } else {
            return stripeIntegrationService.createStripePaymentIntent(request, method, fallbackMsg);
        }
    }

    public java.util.Map<String, String> advanceTestClock(String testClockId) throws StripeException {
        return stripeIntegrationService.advanceTestClock(testClockId);
    }
}
