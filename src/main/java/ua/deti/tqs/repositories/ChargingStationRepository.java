package ua.deti.tqs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.deti.tqs.entities.ChargingStation;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, Integer> {


}
