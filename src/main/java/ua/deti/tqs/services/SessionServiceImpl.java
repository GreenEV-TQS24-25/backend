package ua.deti.tqs.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.Session;
import ua.deti.tqs.entities.Vehicle;
import ua.deti.tqs.repositories.ChargingSpotRepository;
import ua.deti.tqs.repositories.ChargingStationRepository;
import ua.deti.tqs.repositories.SessionRepository;
import ua.deti.tqs.repositories.VehicleRepository;
import ua.deti.tqs.services.interfaces.SessionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class SessionServiceImpl implements SessionService {
     private final SessionRepository sessionRepository;
     private final ChargingSpotRepository chargingSpotRepository;
     private final ChargingStationRepository chargingStationRepository;
     private final VehicleRepository vehicleRepository;

    @Override
    public List<Session> getAllSessionsByStationId(int stationId) {
        log.debug("Fetching all sessions with station id {}", stationId);

        if (!chargingStationRepository.existsById(stationId)) {
            log.debug("The station with id {} does not exist", stationId);
            return List.of();
        }
        List<Session> sessions = sessionRepository.findAllByChargingSpot_Station_Id(stationId).orElse(List.of());

        if (sessions.isEmpty()) {
            log.debug("No sessions found with station id {}", stationId);
        }

        log.debug(
                "Found {} sessions with station id {}", sessions.size(), stationId);
        return sessions;
    }

    @Override
    public List<Session> getAllSessionsByUserId(int userId) {
        log.debug("Fetching all sessions with user id {}", userId);
        List<Session> sessions = sessionRepository.findAllByVehicle_User_Id(userId).orElse(null);

        if (sessions == null) {
            log.debug("No sessions found with user id {}", userId);
            return List.of();
        }

        log.debug(
                "Found {} sessions with user id {}", sessions.size(), userId);
        return sessions;
    }

    @Override
    public Session createSession(int userId, Session session) {
        log.debug("Creating new session {}", session);
        if (session.getVehicle() == null || session.getVehicle().getId() == null) {
            log.debug("Invalid session vehicle, vehicle id is null");
            return null;
        }
        Vehicle vehicle = vehicleRepository.findById(session.getVehicle().getId()).orElse(null);
        if (vehicle == null) {
            log.debug("Invalid vehicle");
            return null;
        } else if (vehicle.getUser().getId() != userId) {
            log.debug("Invalid session vehicle, user id {} does not match vehicle user id {}", userId, vehicle.getUser().getId());
            return null;
        }

        ChargingSpot chargingSpot = chargingSpotRepository.findById(session.getChargingSpot().getId()).orElse(null);

        if (chargingSpot == null) {
            log.debug("Invalid session charging spot, charging spot with id {} not found", session.getChargingSpot().getId());
            return null;
        }

        List<Session> sessions = sessionRepository.findAllByChargingSpot_Id(chargingSpot.getId()).orElse(null);
        if (sessions != null) {
            for (Session s : sessions) {
                Instant startTime = session.getStartTime();
                int duration = session.getDuration();
                if (s.getStartTime().isBefore(startTime.plusSeconds(duration)) && s.getStartTime().plusSeconds(s.getDuration()).isAfter(startTime)) {
                    log.debug("Invalid session charging spot, charging spot with id {} is already in use", session.getChargingSpot().getId());
                    return null;
                }
            }
        }

        int errorCount = 0;

        if (session.getStartTime() == null) {
            log.debug("Invalid session start time, start time is null");
            errorCount++;
        }
        
        if (session.getDuration() <= 0) {
            log.debug("Invalid session duration, duration is less than or equal to 0");
            errorCount++;
        }

        if (errorCount > 0) {
            return null;
        }

        Session newSession = new Session();
        newSession.setVehicle(vehicle);
        newSession.setChargingSpot(chargingSpot);
        newSession.setStartTime(session.getStartTime());
        newSession.setDuration(session.getDuration());

        BigDecimal power = chargingSpot.getPowerKw();
        BigDecimal hours = BigDecimal.valueOf(session.getDuration()).divide(BigDecimal.valueOf(3600), 10, RoundingMode.HALF_UP);
        BigDecimal totalCost = chargingSpot.getPricePerKwh().multiply(power).multiply(hours);
        newSession.setTotalCost(totalCost);

        return sessionRepository.save(newSession);
    }

    @Override
    public boolean deleteSession(int userId, int sessionId) {
        log.debug("Deleting session with id {}", sessionId);
        Session session = sessionRepository.findById(sessionId).orElse(null);

        if (session == null) {
            log.debug("No session found with id {}", sessionId);
            return false;
        }

        if (session.getVehicle().getUser().getId() != userId) {
            log.debug("The user with id {} is not the owner of the session with id {}", userId, sessionId);
            return false;
        }

        sessionRepository.deleteById(sessionId);
        log.debug("Session with id {} deleted", sessionId);
        return true;
    }
}
