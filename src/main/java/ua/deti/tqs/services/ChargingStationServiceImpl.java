package ua.deti.tqs.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.entities.types.ConnectorType;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.ChargingSpotRepository;
import ua.deti.tqs.repositories.ChargingStationRepository;
import ua.deti.tqs.repositories.UserRepository;
import ua.deti.tqs.services.interfaces.ChargingStationService;

@Slf4j
@Service
@AllArgsConstructor
public class ChargingStationServiceImpl implements ChargingStationService {
  private static final String USER_NOT_OPERATOR_MESSAGE = "The user with id {} is not an operator";
  private final ChargingStationRepository chargingStationRepository;
  private final UserRepository userRepository;
  private final ChargingSpotRepository chargingSpotRepository;

  @Override
  public List<ChargingStation> getAllChargingStationsByOperatorId(int operatorId) {
    if (!isOperator(operatorId)) {
      logInvalidOperator(operatorId);
      return Collections.emptyList();
    }
    log.debug("Fetching all charging stations with operator id {}", operatorId);
    List<ChargingStation> chargingStations =
        chargingStationRepository.findAllByOperator_Id(operatorId).orElse(null);

    if (chargingStations == null) {
      log.debug("No charging stations found with operator id {}", operatorId);
      return Collections.emptyList();
    }

    log.debug(
        "Found {} charging stations with operator id {}", chargingStations.size(), operatorId);
    return chargingStations;
  }

  @Override
  public List<ChargingStation> getAllChargingStations() {
    return chargingStationRepository.findAll();
  }

  @Override
  public ChargingStation createChargingStation(ChargingStation chargingStation, int operatorId) {
    // need to grant that the operator is an operator
    if (!isOperator(operatorId)) {
      logInvalidOperator(operatorId);
      return null;
    }

    log.debug("Creating new charging station {}", chargingStation);

    ChargingStation newChargingStation = new ChargingStation();

    int errorCount = 0;
    if (chargingStation.getLat() == null || chargingStation.getLon() == null) {
      log.debug("Invalid charging station coordinates");
      errorCount++;
    }

    if (chargingStation.getName() == null || chargingStation.getName().isEmpty()) {
      log.debug("Invalid charging station name");
      errorCount++;
    }

    if (chargingStation.getOperator() == null) {
      log.debug("Invalid charging station operator, operator is null");
      errorCount++;
    } else if (userRepository.findById(chargingStation.getOperator().getId()).isEmpty()) {
      log.debug("Invalid charging station operator id, operator not found");
      errorCount++;
    } else if (chargingStation.getOperator().getRole() != Role.OPERATOR) {
      log.debug("Invalid charging station operator, not an operator");
      errorCount++;
    } else if (chargingStation.getOperator().getId() != operatorId) {
      log.debug(
          "Invalid charging station operator id, operator id {} does not match the authenticated user id {}",
          chargingStation.getOperator().getId(),
          operatorId);
      errorCount++;
    }

    if (errorCount > 0) return null;

    newChargingStation.setName(chargingStation.getName());
    newChargingStation.setLat(chargingStation.getLat());
    newChargingStation.setLon(chargingStation.getLon());
    newChargingStation.setOperator(chargingStation.getOperator());

    if (chargingStation.getPhotoUrl() != null)
      newChargingStation.setPhotoUrl(chargingStation.getPhotoUrl());

    log.debug("Saving new charging station {}", newChargingStation);
    return chargingStationRepository.save(newChargingStation);
  }

  @Override
  public ChargingStation updateChargingStation(int operatorId, ChargingStation chargingStation) {

    if (!isOperator(operatorId)) {
      logInvalidOperator(operatorId);
      return null;
    }
    log.debug("Updating charging station {}", chargingStation);
    ChargingStation existingChargingStation =
        chargingStationRepository.findById(chargingStation.getId()).orElse(null);

    if (existingChargingStation == null) {
      log.debug("The Charging station with id {} wasn't found", chargingStation.getId());
      return null;
    }

    if (existingChargingStation.getOperator().getId() != operatorId) {
      log.debug("The Charging station doesn't belong to operator with id {}", operatorId);
      return null;
    }

    if (chargingStation.getPhotoUrl() != null)
      existingChargingStation.setPhotoUrl(chargingStation.getPhotoUrl());

    if (chargingStation.getName() != null && !chargingStation.getName().isEmpty())
      existingChargingStation.setName(chargingStation.getName());

    if (chargingStation.getLat() != null) existingChargingStation.setLat(chargingStation.getLat());

    if (chargingStation.getLon() != null) existingChargingStation.setLon(chargingStation.getLon());

    log.debug("Saving updated charging station {}", existingChargingStation);
    return chargingStationRepository.save(existingChargingStation);
  }

  @Override
  public boolean deleteChargingStation(int id, int operatorId) {
    log.debug("Deleting charging station with id {} and operator id {}", id, operatorId);
    ChargingStation chargingStation = chargingStationRepository.findById(id).orElse(null);

    if (chargingStation == null) {
      log.debug("Charging station with id {} not found", id);
      return false;
    }

    if (chargingStation.getOperator().getId() != operatorId) {
      log.debug(
          "Charging station with id {} does not belong to operator with id {}", id, operatorId);
      return false;
    }

    log.debug("Charging station with id {} deleted", id);
    chargingStationRepository.delete(chargingStation);
    return true;
  }

  private boolean isOperator(int id) {
    log.debug("Checking if user with id {} is an operator", id);
    return userRepository.findById(id).map(user -> user.getRole() == Role.OPERATOR).orElse(false);
  }

  private void logInvalidOperator(int operatorId) {
    log.debug(USER_NOT_OPERATOR_MESSAGE, operatorId);
  }

  @Override
  public List<ChargingStation> filterChargingStations(List<ConnectorType> connectorTypes){
    return chargingStationRepository.findAll().stream()
        .filter(station -> getConnectorTypes(station).containsAll(connectorTypes))
        .collect(Collectors.toList());
  }

  private List<ConnectorType> getConnectorTypes(ChargingStation station){
    List<ConnectorType> connectorTypes = new ArrayList<>();
    Optional<List<ChargingSpot>> chargingSpots = chargingSpotRepository.findAllByStation_Id(station.getId());
    if (!chargingSpots.isPresent()){
      return Collections.emptyList();
    }
    for (ChargingSpot chargingSpot : chargingSpots.get()) {
      connectorTypes.add(chargingSpot.getConnectorType());
    }
    return connectorTypes;
  }
}
