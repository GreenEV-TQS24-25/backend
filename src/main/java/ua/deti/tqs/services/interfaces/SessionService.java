package ua.deti.tqs.services.interfaces;

import ua.deti.tqs.entities.Session;

import java.util.List;

public interface SessionService {
    List<Session> getAllSessionsByStationId(int stationId);

    List<Session> getAllSessionsByUserId(int userId);

    Session createSession(int userId, int vehicleId, Session session);

    Session updateSession(int userId, int sessionId, Session session);

    boolean deleteSession(int userId, int sessionId);
}
