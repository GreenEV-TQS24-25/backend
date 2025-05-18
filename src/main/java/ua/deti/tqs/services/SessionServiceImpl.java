package ua.deti.tqs.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.deti.tqs.entities.Session;
import ua.deti.tqs.repositories.ChargingStationRepository;
import ua.deti.tqs.repositories.SessionRepository;
import ua.deti.tqs.services.interfaces.SessionService;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class SessionServiceImpl implements SessionService {
     private final SessionRepository sessionRepository;
     private final ChargingStationRepository chargingStationRepository;
     private final VehicleRepository vehicleRepository;

    @Override
    public List<Session> getAllSessionsByStationId(int stationId) {
        log.debug("Fetching all sessions with station id {}", stationId);

        if (!chargingStationRepository.existsById(stationId)) {
            log.debug("The station with id {} does not exist", stationId);
            return List.of();
        }
        List<Session> sessions = sessionRepository.findAllByChargingSpot_Station_Id(stationId).orElse(null);

        if (sessions == null) {
            log.debug("No sessions found with station id {}", stationId);
            return List.of();
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
    public Session createSession(int userId, int vehicleId, Session session) {
        log.debug("Creating new session {}", session);


        int errorCount = 0;

        if (session.getVehicle() == null) {
            log.debug("Invalid session vehicle, vehicle is null");
            errorCount++;
        } else if (session.getVehicle().getUser().getId() != userId) {
            log.debug("Invalid session vehicle, user id {} does not match vehicle user id {}", userId, session.getVehicle().getUser().getId());
            errorCount++;
        }

        if (session.getChargingSpot() == null) {
            log.debug("Invalid session charging spot, charging spot is null");
            return null;
        }

        if (session.getChargingSpot().getStation() == null) {
            log.debug("Invalid session charging spot station, station is null");
            errorCount++;
        } else if (chargingStationRepository.findById(session.getChargingSpot().getStation().getId()).isEmpty()) {
            log.debug("Invalid session charging spot station, station with id {} not found", session.getChargingSpot().getStation().getId());
            errorCount++;
        }

        if (session.getStartTime() == null) {
            log.debug("Invalid session start time, start time is null");
            errorCount++;
        }

        if (session.getTotalCost() == null) {
            log.debug("Invalid session total cost, total cost is null");
            errorCount++;
        }

        if (errorCount > 0) {
            return null;
        }

        Session newSession = new Session();
        newSession.setVehicle(session.getVehicle());
        newSession.setChargingSpot(session.getChargingSpot());
        newSession.setStartTime(session.getStartTime());
        newSession.setTotalCost(session.getTotalCost());

        return sessionRepository.save(newSession);
    }

    @Override
    public Session updateSession(int userId, int sessionId, Session session) {
        return null;
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



//user_id          INTEGER REFERENCES user_table (id),
//vehicle_id       INTEGER REFERENCES vehicle (id),
//charging_spot_id INTEGER REFERENCES charging_spot (id),
//start_time       TIMESTAMP NOT NULL,
//duration        INTEGER NOT NULL DEFAULT 30,
//total_cost       DECIMAL(8, 2)
