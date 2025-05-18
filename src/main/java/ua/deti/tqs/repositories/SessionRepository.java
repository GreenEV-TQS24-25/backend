package ua.deti.tqs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.deti.tqs.entities.Session;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    Optional<List<Session>> findAllByVehicle_User_Id(Integer userId);

    Optional<List<Session>> findAllByChargingSpot_Station_Id(Integer stationId);

}
