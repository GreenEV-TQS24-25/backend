package ua.deti.tqs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.deti.tqs.entities.Session;
import ua.deti.tqs.services.interfaces.SessionService;
import ua.deti.tqs.utils.Constants;

import java.util.List;

import static ua.deti.tqs.utils.SecurityUtils.getAuthenticatedUser;

@Slf4j
@RestController
@RequestMapping(Constants.API_V1 + "private/session")
@Tag(name = "Session", description = "The Session API")
@AllArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    @GetMapping()
    @Operation(summary = "Get all Sessions by User ID", description = "Fetches a list of all sessions by user id.")
    @ApiResponse(responseCode = "200", description = "List of sessions retrieved successfully")
    @ApiResponse(responseCode = "404", description = "No sessions found")
    public ResponseEntity<List<Session>> getAllSessionsByUserId() {
        int userId = getAuthenticatedUser().getId();
        log.info("Fetching all sessions with user id {}", userId);

        List<Session> sessions = sessionService.getAllSessionsByUserId(userId);

        if (sessions.isEmpty()) {
            log.warn("No sessions found with user id {}", userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("Sessions retrieved successfully");
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }

    @GetMapping("/station/{stationId}")
    @Operation(summary = "Get all Sessions by Station ID", description = "Fetches a list of all sessions by station id.")
    @ApiResponse(responseCode = "200", description = "List of sessions retrieved successfully")
    @ApiResponse(responseCode = "404", description = "No sessions found")
    public ResponseEntity<List<Session>> getAllSessionsByStationId(@PathVariable("stationId") int stationId) {
        log.info("Fetching all sessions with station id {}", stationId);

        List<Session> sessions = sessionService.getAllSessionsByStationId(stationId);

        if (sessions.isEmpty()) {
            log.warn("No sessions found with station id {}", stationId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("Sessions retrieved successfully for station id {}", stationId);
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }

    @PostMapping()
    @Operation(summary = "Create a new Session", description = "Creates a new session.")
    @ApiResponse(responseCode = "200", description = "Session created successfully")
    @ApiResponse(responseCode = "404", description = "No session found")
    public ResponseEntity<Session> createSession(@RequestBody Session session) {
        int userId = getAuthenticatedUser().getId();
        log.info("Creating new session {}", session);

        Session newSession = sessionService.createSession(userId, session);

        if (newSession == null) {
            log.warn("No session found for the user with id {}", userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("Session created successfully");
        return new ResponseEntity<>(newSession, HttpStatus.OK);
    }

    @DeleteMapping()
    @Operation(summary = "Delete a Session", description = "Deletes a session.")
    @ApiResponse(responseCode = "200", description = "Session deleted successfully")
    @ApiResponse(responseCode = "404", description = "No session found")
    public ResponseEntity<Void> deleteSession(int sessionId) {
        log.info("Deleting session with id {}", sessionId);
        int userId = getAuthenticatedUser().getId();

        if (sessionService.deleteSession(userId, sessionId)) {
            log.info("Session deleted successfully");
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            log.warn("No session found with id {}", sessionId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
