package ua.deti.tqs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.services.interfaces.ChargingSpotService;
import ua.deti.tqs.utils.Constants;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(Constants.API_V1 + "charging-spots")
@Tag(name = "Charging Spots", description = "The Charging Spots API")
@AllArgsConstructor
public class ChargingSpotController {
     private final ChargingSpotService chargingSpotService;

     @GetMapping("{stationId}")
     @Operation(summary = "Get all Charging Spots by Station ID", description = "Fetches a list of all chargingSpot by station id.")
     @ApiResponse(responseCode = "200", description = "List of chargingSpot retrieved successfully")
     @ApiResponse(responseCode = "404", description = "No chargingSpot found")
     public ResponseEntity<List<ChargingSpot>> getAllChargingSpotsByStationId(@PathVariable("stationId") int stationId) {
          log.info("Fetching all chargingSpot with station id {}", stationId);

          List<ChargingSpot> chargingSpot = chargingSpotService.getAllChargingSpotsByStationId(stationId);

          if (chargingSpot.isEmpty()) {
               log.warn("No chargingSpot found with station id {}", stationId);
               return new ResponseEntity<>(HttpStatus.NOT_FOUND);
          }
          log.info("ChargingSpots retrieved successfully for operator id {}", stationId);
          return new ResponseEntity<>(chargingSpot, HttpStatus.OK );
     }

     @PostMapping("{operatorId}")
     @Operation(summary = "Create a new Charging Spot", description = "Creates a new chargingSpot.")
     @ApiResponse(responseCode = "200", description = "ChargingSpot created successfully")
     @ApiResponse(responseCode = "404", description = "No chargingSpot found")
     public ResponseEntity<ChargingSpot> createChargingSpot(@PathVariable("operatorId") int operatorId, @RequestBody ChargingSpot chargingSpot) {
        log.info("Creating new chargingSpot {}", chargingSpot);

        ChargingSpot newChargingSpot = chargingSpotService.createChargingSpot(operatorId, chargingSpot);

        if (newChargingSpot == null) {
             log.warn("No chargingSpot found with operator id {}", operatorId);
             return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("ChargingSpots created successfully for operator id {}", operatorId);
        return new ResponseEntity<>(newChargingSpot, HttpStatus.OK );
     }

     @PutMapping("{operatorId}")
     @Operation(summary = "Update a Charging Spot", description = "Updates a chargingSpot.")
     @ApiResponse(responseCode = "200", description = "ChargingSpot updated successfully")
     @ApiResponse(responseCode = "404", description = "No chargingSpot found")
     public ResponseEntity<ChargingSpot> updateChargingSpot(@PathVariable("operatorId") int operatorId, @RequestBody ChargingSpot chargingSpot) {
       log.info("Updating chargingSpot {}", chargingSpot);

       ChargingSpot updatedChargingSpot = chargingSpotService.updateChargingSpot(operatorId, chargingSpot);

       if (updatedChargingSpot == null) {
           log.warn("No chargingSpot found for the  with id {}", operatorId);
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }
       log.info("ChargingSpots updated successfully for operator id {}", operatorId);
       return new ResponseEntity<>(updatedChargingSpot, HttpStatus.OK );
     }

     @DeleteMapping("{id}/{operatorId}")
     @Operation(summary = "Delete a Charging Spot", description = "Deletes a chargingSpot.")
     @ApiResponse(responseCode = "200", description = "ChargingSpot deleted successfully")
     @ApiResponse(responseCode = "404", description = "No chargingSpot found")
     public ResponseEntity<Void> deleteChargingSpot(@PathVariable("id") int id, @PathVariable("operatorId") int operatorId) {
       log.info("Deleting chargingSpot with id {} and operator id {}", id, operatorId);

       boolean deleted = chargingSpotService.deleteChargingSpot(id, operatorId);

       if (!deleted) {
           log.warn("No chargingSpot found with id {} and operator id {}", id, operatorId);
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }
       log.info("ChargingSpot deleted successfully with id {} and operator id {}", id, operatorId);
       return new ResponseEntity<>(HttpStatus.OK);
     }
}
