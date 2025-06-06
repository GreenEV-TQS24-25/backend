package ua.deti.tqs.services.interfaces;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import ua.deti.tqs.entities.Session;

public interface PaymentService {
  public PaymentIntent createPaymentIntent(Session session) throws StripeException;

  public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException;

  public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException;
}
