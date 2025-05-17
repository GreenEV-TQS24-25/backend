package ua.deti.tqs.services.interfaces;

import ua.deti.tqs.entities.ChargingStation;

import java.util.List;

public interface ChargingStationService {
    List<ChargingStation> getAllChargingStationsByOperatorId(int operatorId);

    List<ChargingStation> getAllChargingStations();

    List<ChargingStation> getFilteredChargingStations(List<Integer> operatorIds);

    ChargingStation createChargingStation(ChargingStation chargingStation);

    ChargingStation updateChargingStation(int operatorId, ChargingStation chargingStation);

    boolean deleteChargingStation(int id, int operatorId);

}
