package ua.deti.tqs.services.interfaces;

import ua.deti.tqs.entities.ChargingStation;

import java.util.List;

public interface ChargingStationService {
    List<ChargingStation> getAllChargingStationsByOperatorId(int operatorId);

    List<ChargingStation> getAllChargingStations();

    ChargingStation createChargingStation(ChargingStation chargingStation, int operatorId);

    ChargingStation updateChargingStation(int operatorId, ChargingStation chargingStation);

    boolean deleteChargingStation(int id, int operatorId);

}
