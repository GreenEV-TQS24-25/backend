package ua.deti.tqs.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.deti.tqs.entities.Session;
import ua.deti.tqs.services.interfaces.PaymentService;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

  @Value("${stripe.secret-key}")
  private String secretKey;

  @PostConstruct
  public void init() {
    Stripe.apiKey = secretKey;
  }

  @Override
  public PaymentIntent createPaymentIntent(Session session) throws StripeException {
    log.debug("Creating payment intent for session {}", session.getId());

    // Converter para centavos (Stripe trabalha com a menor unidade da moeda)
    long amountInCents = session.getTotalCost().multiply(new BigDecimal("100")).longValue();

    PaymentIntentCreateParams params =
        PaymentIntentCreateParams.builder()
            .setAmount(amountInCents)
            .setCurrency("eur")
            .setDescription("Charging session for " + session.getDuration() + " minutes")
            .putMetadata("session_id", session.getId().toString())
            .putMetadata("user_id", session.getVehicle().getUser().getId().toString())
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build())
            .build();

    return PaymentIntent.create(params);
  }

  @Override
  public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
    log.debug("Confirming payment intent {}", paymentIntentId);
    PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
    return paymentIntent.confirm();
  }

  @Override
  public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
    return PaymentIntent.retrieve(paymentIntentId);
  }
}
