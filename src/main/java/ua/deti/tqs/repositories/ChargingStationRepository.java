package ua.deti.tqs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.deti.tqs.entities.ChargingStation;

import java.util.List;
import java.util.Optional;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, Integer> {
    Optional<List<ChargingStation>> findAllByOperator_Id(int operatorId);

}
