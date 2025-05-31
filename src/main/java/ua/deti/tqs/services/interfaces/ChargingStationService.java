package ua.deti.tqs.services.interfaces;

import ua.deti.tqs.dto.StationsSpots;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.entities.types.ConnectorType;

import java.util.List;

public interface ChargingStationService {
    List<StationsSpots> getAllChargingStationsByOperatorId(int operatorId);

    List<StationsSpots> getAllChargingStations();

    ChargingStation createChargingStation(ChargingStation chargingStation, int operatorId);

    ChargingStation updateChargingStation(int operatorId, ChargingStation chargingStation);

    boolean deleteChargingStation(int id, int operatorId);

    List<ChargingStation> filterChargingStations(List<ConnectorType> connectorTypes);
}
