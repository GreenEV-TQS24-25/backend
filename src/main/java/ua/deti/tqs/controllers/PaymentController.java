package ua.deti.tqs.controllers;

import static ua.deti.tqs.utils.SecurityUtils.getAuthenticatedUser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.deti.tqs.entities.Session;
import ua.deti.tqs.entities.types.PaymentState;
import ua.deti.tqs.services.interfaces.PaymentService;
import ua.deti.tqs.services.interfaces.SessionService;
import ua.deti.tqs.utils.Constants;

@RestController
@RequestMapping(Constants.API_V1)
@Tag(name = "Payment", description = "The Payment API")
@Slf4j
public class PaymentController {

  private final PaymentService paymentService;
  private final SessionService sessionService;

  @Value("${stripe.webhook-secret}")
  String endpointSecret;

  public PaymentController(PaymentService paymentService, SessionService sessionService) {
    this.paymentService = paymentService;
    this.sessionService = sessionService;
  }

  @PostMapping("/private/payment/create-intent/{sessionId}")
  @Operation(
      summary = "Create Payment Intent",
      description = "Creates a payment intent for a session")
  public ResponseEntity<Map<String, String>> createPaymentIntent(@PathVariable int sessionId) {
    try {
      int userId = getAuthenticatedUser().getId();
      Session session =
          sessionService.getAllSessionsByUserId(userId).stream()
              .filter(s -> s.getId() == sessionId)
              .findFirst()
              .orElse(null);

      if (session == null || session.getVehicle().getUser().getId() != userId) {
        return ResponseEntity.notFound().build();
      }
      //
      //      if (session.getPaymentStatus() != PaymentState.PENDING) {
      //        return ResponseEntity.badRequest().build();
      //      }

      PaymentIntent paymentIntent = paymentService.createPaymentIntent(session);
      if (paymentIntent == null) {
        return ResponseEntity.internalServerError().build();
      }
      sessionService.updatePaymentStatus(userId, sessionId, PaymentState.PROCESSING);

      Map<String, String> response = new HashMap<>();
      response.put("clientSecret", paymentIntent.getClientSecret());
      response.put("paymentIntentId", paymentIntent.getId());

      return ResponseEntity.ok(response);

    } catch (StripeException e) {
      log.error("Error creating payment intent: {}", e.getMessage());
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping("/public/payment/webhook")
  public ResponseEntity<String> handleWebhook(
      @RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
    try {
      Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

      switch (event.getType()) {
        case "payment_intent.succeeded":
          log.info("Payment succeeded for event: {}", event.getId());
          handlePaymentSucceeded(event);
          break;
        case "payment_intent.payment_failed":
          // Handle payment failure
          log.warn("Payment failed for event: {}", event.getId());
          // handlePaymentFailed(event);
          break;
        case "payment_intent.canceled":
          // Handle payment cancellation
          log.warn("Payment canceled for event: {}", event.getId());
          // handlePaymentCanceled(event);
          break;
        case "payment_intent.processing":

        default:
          log.info("Unhandled event type: {}", event.getType());
      }

      return ResponseEntity.ok("Success");

    } catch (SignatureVerificationException | JsonProcessingException e) {
      log.error("Invalid signature: {}", e.getMessage());
      return ResponseEntity.badRequest().body("Invalid signature");
    }
  }

  private void handlePaymentSucceeded(Event event) throws JsonProcessingException {
    String eventString = event.toJson();
    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> map =
        objectMapper.readValue(eventString, new TypeReference<Map<String, Object>>() {});
    if (map != null) {
      // SEE ME LATER
      Map<String, String> metadata =
          (Map<String, String>)
              ((Map<String, Object>) ((Map<String, Object>) map.get("data")).get("object"))
                  .get("metadata");

      String sessionId = metadata.get("session_id");
      log.info("Payment succeeded for session: {}", sessionId);
      if (sessionId != null) {
        Session session = sessionService.getSessionById(Integer.parseInt(sessionId));

        if (session != null) {
          sessionService.updatePaymentStatus(
              session.getVehicle().getUser().getId(), session.getId(), PaymentState.COMPLETED);
          log.info("Payment completed for session {}", sessionId);
        }
      }
    }
  }
}
