package com.example.stripeapi.service;

import com.example.stripeapi.dto.PaymentRequest;
import com.example.stripeapi.dto.PaymentResponse;
import com.example.stripeapi.model.User;
import com.example.stripeapi.model.UserPaymentMethod;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.model.testhelpers.TestClock;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.testhelpers.TestClockAdvanceParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class StripeIntegrationService {

    private static final String DEFAULT_PRICE_ID = "price_1U00LKGXjoXyEWxkEi3W3duq";
    private static final long TEST_CYCLE_DAYS_IN_SECONDS = Duration.ofDays(32).getSeconds();

    private final TimeSimulationService timeSimulationService;

    public StripeIntegrationService(TimeSimulationService timeSimulationService) {
        this.timeSimulationService = timeSimulationService;
    }

    public PaymentResponse createStripePaymentIntent(PaymentRequest request, UserPaymentMethod method, String fallbackMsg) throws StripeException {
        PaymentIntentCreateParams.Builder paramsBuilder =
                PaymentIntentCreateParams.builder()
                        .setAmount(request.getAmount())
                        .setCurrency(request.getCurrency())
                        .setPaymentMethod(method.getStripeToken())
                        .setConfirm(true)
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                        .build()
                        );

        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(request.getIdempotencyKey())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build(), requestOptions);
        String status = fallbackMsg != null ? paymentIntent.getStatus() + " (Usando Respaldo)" : paymentIntent.getStatus();
        
        return PaymentResponse.builder()
                .clientSecret(paymentIntent.getClientSecret())
                .paymentIntentId(paymentIntent.getId())
                .status(status)
                .fallbackLog(fallbackMsg)
                .build();
    }

    public PaymentResponse createStripeSubscription(PaymentRequest request, User user, UserPaymentMethod method, String fallbackMsg) throws StripeException {
        Optional<String> testClockIdOpt = timeSimulationService.getTestClockId(user.getName());
        String oldToken = timeSimulationService.adaptPaymentMethodToken(method.getStripeToken());
        
        Map<String, Object> pmParamsMap = Map.of(
            "type", "card",
            "card", Map.of("token", oldToken)
        );
        
        PaymentMethod concretePm = PaymentMethod.create(pmParamsMap);

        CustomerCreateParams.Builder customerParamsBuilder = CustomerCreateParams.builder()
            .setEmail(user.getEmail())
            .setName(user.getName())
            .setPaymentMethod(concretePm.getId()); 
            
        testClockIdOpt.ifPresent(customerParamsBuilder::setTestClock);

        Customer customer = Customer.create(customerParamsBuilder.build());

        SubscriptionCreateParams subParams = SubscriptionCreateParams.builder()
            .setCustomer(customer.getId())
            .setDefaultPaymentMethod(concretePm.getId())
            .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.ERROR_IF_INCOMPLETE)
            .addItem(
                SubscriptionCreateParams.Item.builder()
                    .setPrice(DEFAULT_PRICE_ID) 
                    .build()
            )
            .build();

        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(request.getIdempotencyKey())
                .build();

        Subscription subscription = Subscription.create(subParams, requestOptions);
        String status = fallbackMsg != null ? subscription.getStatus() + " (Usando Respaldo)" : subscription.getStatus();
        
        return PaymentResponse.builder()
                .paymentIntentId(subscription.getId())
                .status(status)
                .fallbackLog(fallbackMsg)
                .testClockId(testClockIdOpt.orElse(null))
                .build();
    }

    public Map<String, String> advanceTestClock(String testClockId) throws StripeException {
        TestClock testClock = TestClock.retrieve(testClockId);
        
        long newFrozenTime = testClock.getFrozenTime() + TEST_CYCLE_DAYS_IN_SECONDS;
        
        TestClockAdvanceParams params = TestClockAdvanceParams.builder()
            .setFrozenTime(newFrozenTime)
            .build();
            
        testClock.advance(params);
        
        return Map.of(
            "status", "Reloj adelantado 30 días exitosamente. Esperando resolución asíncrona vía Webhook...",
            "newTime", new Date(newFrozenTime * 1000).toString()
        );
    }
}
