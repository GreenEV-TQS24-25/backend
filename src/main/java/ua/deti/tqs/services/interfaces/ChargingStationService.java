package ua.deti.tqs.services.interfaces;

import ua.deti.tqs.entities.ChargingStation;

public interface ChargingStationService {
    ChargingStation getChargingStationById(int id);

    ChargingStation createChargingStation(ChargingStation chargingStation);

    ChargingStation updateChargingStation(int id, ChargingStation chargingStation);

    void deleteChargingStation(int id);

}
