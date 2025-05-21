package ua.deti.tqs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.deti.tqs.entities.ChargingSpot;

import java.util.List;
import java.util.Optional;

public interface ChargingSpotRepository extends JpaRepository<ChargingSpot, Integer> {
    Optional<List<ChargingSpot>> findAllByStation_Id(int stationId);

}
