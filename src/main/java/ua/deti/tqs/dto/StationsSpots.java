package ua.deti.tqs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.ChargingStation;

import java.util.List;

@Getter
@AllArgsConstructor
public class StationsSpots {
    private ChargingStation chargingStation;
    private List<ChargingSpot> spots;
}