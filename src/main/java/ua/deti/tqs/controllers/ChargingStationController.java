package ua.deti.tqs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.services.interfaces.ChargingStationService;
import ua.deti.tqs.utils.Constants;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(Constants.API_V1 + "charging-stations")
@Tag(name = "Charging Stations", description = "The Charging Stations API")
@AllArgsConstructor
public class ChargingStationController {
    private final ChargingStationService chargingStationService;

    @GetMapping("all")
    @Operation(summary = "Get all Charging Stations", description = "Fetches a list of all chargingStation.")
    @ApiResponse(responseCode = "200", description = "List of chargingStation retrieved successfully")
    @ApiResponse(responseCode = "404", description = "No chargingStation found")
    public ResponseEntity<List<ChargingStation>> getAllChargingStations() {
        log.info("Fetching all chargingStation");

        List<ChargingStation> chargingStation = chargingStationService.getAllChargingStations();

        if (chargingStation.isEmpty()) {
            log.warn("No chargingStation found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
        log.info("ChargingStations retrieved successfully");
        return new ResponseEntity<>(chargingStation, HttpStatus.OK );
    }

    @GetMapping("all/{operatorId}")
    @Operation(summary = "Get all Charging Stations by Operator ID", description = "Fetches a list of all chargingStation by operator id.")
    @ApiResponse(responseCode = "200", description = "List of chargingStation retrieved successfully")
    @ApiResponse(responseCode = "404", description = "No chargingStation found")
    public ResponseEntity<List<ChargingStation>> getAllChargingStationsByOperatorId(@PathVariable("operatorId") int operatorId) {
        log.info("Fetching all chargingStation with operator id {}", operatorId);

        List<ChargingStation> chargingStation = chargingStationService.getAllChargingStationsByOperatorId(operatorId);

        if (chargingStation.isEmpty()) {
            log.warn("No chargingStation found with operator id {}", operatorId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("ChargingStations retrieved successfully for operator id {}", operatorId);
        return new ResponseEntity<>(chargingStation, HttpStatus.OK );
    }

    @PostMapping()
    @Operation(summary = "Create a new Charging Station", description = "Creates a new chargingStation.")
    @ApiResponse(responseCode = "201", description = "ChargingStation created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid chargingStation data")
    public ResponseEntity<ChargingStation> createChargingStation(@RequestBody ChargingStation chargingStation) {
        log.info("Creating new chargingStation {}", chargingStation);

        ChargingStation newChargingStation = chargingStationService.createChargingStation(chargingStation);

        if (newChargingStation == null) {
            log.warn("Invalid chargingStation data");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("ChargingStation created successfully");
        return new ResponseEntity<>(newChargingStation, HttpStatus.CREATED);
    }

    @PutMapping("{operatorId}")
    @Operation(summary = "Update a Charging Station", description = "Updates a chargingStation.")
    @ApiResponse(responseCode = "200", description = "ChargingStation updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid chargingStation data")
    public ResponseEntity<ChargingStation> updateChargingStation(@PathVariable("operatorId") int operatorId, @RequestBody ChargingStation chargingStation) {
        log.info("Updating chargingStation {}", chargingStation);

        ChargingStation updatedChargingStation = chargingStationService.updateChargingStation(operatorId, chargingStation);

        if (updatedChargingStation == null) {
            log.warn("Invalid chargingStation");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("ChargingStation updated successfully");
        return new ResponseEntity<>(updatedChargingStation, HttpStatus.OK);
    }

    @DeleteMapping("{id}/{operatorId}")
    @Operation(summary = "Delete a Charging Station", description = "Deletes a chargingStation.")
    @ApiResponse(responseCode = "200", description = "ChargingStation deleted successfully")
    @ApiResponse(responseCode = "404", description = "ChargingStation not found")
    public ResponseEntity<Void> deleteChargingStation(@PathVariable("id") int id, @PathVariable("operatorId") int operatorId) {
        log.info("Deleting chargingStation with id {} and operator id {}", id, operatorId);

        if (chargingStationService.deleteChargingStation(id, operatorId)) {
            log.info("ChargingStation deleted successfully");
            return new ResponseEntity<>(HttpStatus.OK);
        }
        log.warn("ChargingStation not found");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
