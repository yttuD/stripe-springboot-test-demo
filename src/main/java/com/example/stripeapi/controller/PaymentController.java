package com.example.stripeapi.controller;

import com.example.stripeapi.dto.PaymentRequest;
import com.example.stripeapi.dto.PaymentResponse;
import com.example.stripeapi.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@Valid @RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = paymentService.createPaymentIntent(request);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                "error", e.getMessage(),
                "type", e.getStripeError() != null ? e.getStripeError().getType() : "unknown_error"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Error interno del servidor", "details", e.getMessage()));
        }
    }

    @PostMapping("/advance-clock")
    public ResponseEntity<?> advanceClock(@Valid @RequestBody com.example.stripeapi.dto.ClockRequest request) {
        try {
            java.util.Map<String, String> response = paymentService.advanceTestClock(request.getTestClockId());
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                "error", e.getMessage(),
                "type", e.getStripeError() != null ? e.getStripeError().getType() : "unknown_error"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Error interno del servidor", "details", e.getMessage()));
        }
    }
    
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        java.util.Map<String, String> errors = new java.util.HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", "Validación fallida", "details", errors));
    }
}
