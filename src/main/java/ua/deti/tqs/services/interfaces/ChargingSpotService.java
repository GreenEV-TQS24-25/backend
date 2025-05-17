package ua.deti.tqs.services.interfaces;

import ua.deti.tqs.entities.ChargingSpot;

import java.util.List;

public interface ChargingSpotService {
    List<ChargingSpot> getAllChargingSpotsByStationId(int stationId);

    ChargingSpot createChargingSpot(int operatorId, ChargingSpot chargingSpot);

    ChargingSpot updateChargingSpot(int operatorId, ChargingSpot chargingSpot);

    boolean deleteChargingSpot(int id, int operatorId);
}