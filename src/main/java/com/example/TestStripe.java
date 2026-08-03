package com.example;
import com.stripe.Stripe;
import com.stripe.model.PaymentMethod;

public class TestStripe {
    public static void main(String[] args) throws Exception {
        Stripe.apiKey = "sk_test_51... (will be provided by the properties)";
        // We need the API key from application-secrets.properties
    }
}
