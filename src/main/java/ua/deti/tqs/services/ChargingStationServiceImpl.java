package ua.deti.tqs.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.services.interfaces.ChargingStationService;

@Service
@AllArgsConstructor
public class ChargingStationServiceImpl implements ChargingStationService {


    @Override
    public ChargingStation getChargingStationById(int id) {
        return null;
    }

    @Override
    public ChargingStation createChargingStation(ChargingStation chargingStation) {
        return null;
    }

    @Override
    public ChargingStation updateChargingStation(int id, ChargingStation chargingStation) {
        return null;
    }

    @Override
    public void deleteChargingStation(int id) {

    }
}
