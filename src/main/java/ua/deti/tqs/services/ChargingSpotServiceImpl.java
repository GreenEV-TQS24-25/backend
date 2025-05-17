package ua.deti.tqs.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.repositories.ChargingSpotRepository;
import ua.deti.tqs.repositories.ChargingStationRepository;
import ua.deti.tqs.services.interfaces.ChargingSpotService;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ChargingSpotServiceImpl implements ChargingSpotService {
     private final ChargingSpotRepository chargingSpotRepository;
     private final ChargingStationRepository chargingStationRepository;

    @Override
    public List<ChargingSpot> getAllChargingSpotsByStationId(int stationId) {
        log.debug("Fetching all charging spots with station id {}", stationId);
        List<ChargingSpot>  chargingSpots = chargingSpotRepository
                .findAllByStation_Id(stationId)
                .orElse(null);

        if (chargingSpots == null) {
            log.debug("No charging spots found with station id {}", stationId);
            return List.of();
        }

        log.debug("Found {} charging spots with station id {}", chargingSpots.size(), stationId);
        return chargingSpots;
    }

    @Override
    public ChargingSpot createChargingSpot(int operatorId, ChargingSpot chargingSpot) {
        log.debug("Creating new charging spot {}", chargingSpot);

        if (chargingSpot.getStation() == null) {
            log.debug("Invalid charging spot station, station is null");
            return null;
        }

        ChargingStation station = chargingStationRepository
                .findById(chargingSpot.getStation().getId())
                .orElse(null);

        if (station == null) {
            log.debug("Invalid charging spot station, station with id {} not found", chargingSpot.getStation().getId());
            return null;
        }

        if (station.getOperator().getId() != operatorId) {
            log.debug("Invalid charging spot station, operator id {} does not match station operator id {}", operatorId, station.getOperator().getId());
            return null;
        }

        ChargingSpot newChargingSpot = new ChargingSpot();
        int errorCount = 0;

        if (chargingSpot.getPowerKw() == null) {
            log.debug("Invalid charging spot power");
            errorCount++;
        }
        if (chargingSpot.getPricePerKwh() == null) {
            log.debug("Invalid charging spot price");
            errorCount++;
        }

        if (errorCount > 0) {
            return null;
        }

        newChargingSpot.setStation(station);
        newChargingSpot.setChargingVelocity(chargingSpot.getChargingVelocity());
        newChargingSpot.setPricePerKwh(chargingSpot.getPricePerKwh());

        if (chargingSpot.getConnectorType() != null)
            newChargingSpot.setConnectorType(chargingSpot.getConnectorType());

        if (chargingSpot.getState() != null)
            newChargingSpot.setState(chargingSpot.getState());

        if (chargingSpot.getChargingVelocity() != null)
            newChargingSpot.setChargingVelocity(chargingSpot.getChargingVelocity());

        log.debug("Saving new charging spot {}", newChargingSpot);
        return chargingSpotRepository.save(newChargingSpot);
    }

    @Override
    public ChargingSpot updateChargingSpot(int operatorId, ChargingSpot chargingSpot) {
        log.debug("Updating charging spot {}", chargingSpot);
        ChargingSpot existingChargingSpot = chargingSpotRepository.findById(chargingSpot.getId()).orElse(null);

        if (existingChargingSpot == null) {
            log.debug("Charging spot not found");
            return null;
        }

        if (existingChargingSpot.getStation().getOperator().getId() != operatorId) {
            log.debug("Charging spot doesn't belong to operator with id {}", operatorId);
            return null;
        }

        if (chargingSpot.getPowerKw() != null)
            existingChargingSpot.setPowerKw(chargingSpot.getPowerKw());

        if (chargingSpot.getPricePerKwh() != null)
            existingChargingSpot.setPricePerKwh(chargingSpot.getPricePerKwh());

        if (chargingSpot.getConnectorType() != null)
            existingChargingSpot.setConnectorType(chargingSpot.getConnectorType());

        if (chargingSpot.getState() != null)
            existingChargingSpot.setState(chargingSpot.getState());

        log.debug("Saving updated charging spot {}", existingChargingSpot);
        return chargingSpotRepository.save(existingChargingSpot);
    }

    @Override
    public boolean deleteChargingSpot(int id, int operatorId) {
        log.debug("Deleting charging spot with id {} and operator id {}", id, operatorId);
        ChargingSpot chargingSpot = chargingSpotRepository.findById(id).orElse(null);

        if (chargingSpot == null) {
            log.debug("Charging spot with id {} not found", id);
            return false;
        }

        if (chargingSpot.getStation().getOperator().getId() != operatorId) {
            log.debug("Charging spot with id {} does not belong to operator with id {}", id, operatorId);
            return false;
        }

        chargingSpotRepository.delete(chargingSpot);
        log.debug("Charging spot with id {} deleted successfully", id);
        return true;
    }

}


