package ua.deti.tqs.services.interfaces;

import java.util.List;
import ua.deti.tqs.entities.Session;
import ua.deti.tqs.entities.types.PaymentState;

public interface SessionService {
  List<Session> getAllSessionsByStationId(int stationId);

  List<Session> getAllSessionsByUserId(int userId);

  Session createSession(int userId, Session session);

  boolean deleteSession(int userId, int sessionId);

  Session updatePaymentStatus(int userId, int sessionId, PaymentState paymentStatus);

  Session getSessionById(int sessionId);
}
