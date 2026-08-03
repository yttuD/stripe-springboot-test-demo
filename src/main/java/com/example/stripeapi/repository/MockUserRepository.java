package com.example.stripeapi.repository;

import com.example.stripeapi.model.User;
import com.example.stripeapi.model.UserPaymentMethod;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MockUserRepository {

    private final List<User> users = new ArrayList<>();

    public MockUserRepository() {
        users.add(new User("u_1", "Alice Smith", "alice@example.com", List.of(
                new UserPaymentMethod("pm_1_1", "pm_card_visa", "Visa terminada en 4242", true, true),
                new UserPaymentMethod("pm_1_2", "pm_card_chargeDeclined", "Mastercard terminada en 0002", false, false)
        )));

        users.add(new User("u_2", "Bob Jones", "bob@example.com", List.of(
                new UserPaymentMethod("pm_2_1", "pm_card_visa", "Amex terminada en 4242", true, true),
                new UserPaymentMethod("pm_2_2", "pm_card_chargeDeclinedFraudulent", "Visa terminada en 0010 (Fraude)", false, false)
        )));

        users.add(new User("u_3", "Charlie Brown", "charlie@example.com", List.of(
                new UserPaymentMethod("pm_3_1", "pm_card_visa", "Discover terminada en 4242", true, true),
                new UserPaymentMethod("pm_3_2", "pm_card_chargeDeclined", "Mastercard terminada en 9999", false, false)
        )));
    }

    public List<User> findAll() {
        return users;
    }

    public Optional<User> findById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }
}
