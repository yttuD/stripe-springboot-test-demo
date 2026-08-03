package com.example.stripeapi.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@Slf4j
public class StripeWebhookController {

    // En un entorno real, este secreto se obtiene del Dashboard de Stripe (sección Webhooks)
    @Value("${stripe.webhook.secret:whsec_mock_secret_for_demo}")
    private String endpointSecret;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        Event event;

        try {
            // Verificar criptográficamente que la petición viene de Stripe (previene ataques)
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.error("Firma de Stripe inválida: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Error inesperado al parsear el webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        // Manejamos los eventos asíncronos (por ejemplo, después de que advanceTestClock haga pasar el tiempo)
        switch (event.getType()) {
            case "invoice.payment_succeeded":
                EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                if (dataObjectDeserializer.getObject().isPresent()) {
                    Invoice invoice = (Invoice) dataObjectDeserializer.getObject().get();
                    log.info("✅ Webhook Asíncrono Recibido: El cobro para la suscripción {} ha sido exitoso.", invoice.getSubscription());
                    log.info("-> Aquí notificaríamos al frontend (vía WebSockets) o actualizaríamos el estado en la base de datos.");
                }
                break;
                
            case "invoice.payment_failed":
                log.warn("❌ Webhook Asíncrono Recibido: El cobro de renovación falló. Suspendiendo cuenta...");
                log.info("-> Aquí podríamos enviar un correo al usuario pidiendo que actualice su tarjeta.");
                break;
                
            default:
                log.debug("Evento recibido pero no manejado: {}", event.getType());
        }

        // Siempre debemos devolver 200 OK rápidamente para que Stripe sepa que recibimos el mensaje
        return ResponseEntity.ok("Success");
    }
}
