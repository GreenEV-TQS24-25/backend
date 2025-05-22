package ua.deti.tqs.controllers;

import static ua.deti.tqs.utils.SecurityUtils.getAuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.deti.tqs.entities.Vehicle;
import ua.deti.tqs.services.interfaces.VehicleService;
import ua.deti.tqs.utils.Constants;

@Slf4j
@RestController
@RequestMapping(Constants.API_V1 + "private/vehicles")
@Tag(name = "Vehicle", description = "The Vehicle API")
@AllArgsConstructor
public class VehicleController {
  private final VehicleService vehicleService;

  @GetMapping()
  @Operation(
      summary = "Get all Vehicles by User ID",
      description = "Fetches a list of all vehicles by user id.")
  @ApiResponse(responseCode = "200", description = "List of vehicles retrieved successfully")
  @ApiResponse(responseCode = "404", description = "No vehicles found")
  public ResponseEntity<List<Vehicle>> getAllVehiclesByUserId() {
    int userId = getAuthenticatedUser().getId();
    log.info("Fetching all vehicles with user id {}", userId);

    List<Vehicle> vehicles = vehicleService.getAllVehiclesByUserId(userId);

    if (vehicles.isEmpty()) {
      log.warn("No vehicles found with user id {}", userId);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    log.info("Vehicles retrieved successfully");
    log.info("Found {} vehicles with user id {}", vehicles, userId);
    return new ResponseEntity<>(vehicles, HttpStatus.OK);
  }

  @PostMapping()
  @Operation(summary = "Create a new Vehicle", description = "Creates a new vehicle.")
  @ApiResponse(responseCode = "200", description = "Vehicle created successfully")
  @ApiResponse(responseCode = "404", description = "No vehicle found")
  public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
    int userId = getAuthenticatedUser().getId();
    log.info("Creating new vehicle {}", vehicle);

    Vehicle newVehicle = vehicleService.createVehicle(vehicle, getAuthenticatedUser());

    if (newVehicle == null) {
      log.warn("No vehicle found for the user with id {}", userId);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    log.info("Vehicle created successfully");
    return new ResponseEntity<>(newVehicle, HttpStatus.OK);
  }

  @PutMapping()
  @Operation(summary = "Update a Vehicle", description = "Updates a vehicle.")
  @ApiResponse(responseCode = "200", description = "Vehicle updated successfully")
  @ApiResponse(responseCode = "404", description = "No vehicle found")
  public ResponseEntity<Vehicle> updateVehicle(@RequestBody Vehicle vehicle) {
    log.info("Updating vehicle {}", vehicle);
    int userId = getAuthenticatedUser().getId();
    Vehicle updatedVehicle = vehicleService.updateVehicle(userId, vehicle);
    if (updatedVehicle == null) {
      log.warn("No vehicle found with user id {}", userId);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    log.info("Vehicle updated successfully");
    return new ResponseEntity<>(updatedVehicle, HttpStatus.OK);
  }

  @DeleteMapping("/{vehicleId}")
  @Operation(summary = "Delete a Vehicle", description = "Deletes a vehicle.")
  @ApiResponse(responseCode = "200", description = "Vehicle deleted successfully")
  @ApiResponse(responseCode = "404", description = "No vehicle found")
  public ResponseEntity<Void> deleteVehicle(@PathVariable("vehicleId") int vehicleId) {
    int userId = getAuthenticatedUser().getId();
    log.info("Deleting vehicle with id {}", vehicleId);

    if (vehicleService.deleteVehicle(vehicleId, userId)) {
      log.info("Vehicle deleted successfully");
      return new ResponseEntity<>(HttpStatus.OK);
    }
    log.warn("No vehicle found with id {}", vehicleId);
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }
}
