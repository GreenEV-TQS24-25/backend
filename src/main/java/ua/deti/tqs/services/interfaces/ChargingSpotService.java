package ua.deti.tqs.services.interfaces;

import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.types.SpotState;

import java.util.List;

public interface ChargingSpotService {
    List<ChargingSpot> getAllChargingSpotsByStationId(int stationId);

    ChargingSpot createChargingSpot(int operatorId, ChargingSpot chargingSpot);

    ChargingSpot updateChargingSpot(int operatorId, ChargingSpot chargingSpot);

    boolean deleteChargingSpot(int id, int operatorId);

    boolean updateChargingSpotStatus(int id, SpotState status, int userId);
}