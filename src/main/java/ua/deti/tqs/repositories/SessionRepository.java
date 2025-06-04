package ua.deti.tqs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.deti.tqs.entities.Session;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    Optional<List<Session>> findAllByVehicle_User_Id(Integer userId);
    Optional<List<Session>> findAllByChargingSpot_Station_Id(Integer stationId);
    Optional<List<Session>> findAllByChargingSpot_Id(Integer chargingSpotId);


    @Query("SELECT s FROM Session s " +
            "WHERE s.chargingSpot.id = :spotId " +
            "AND s.startTime <= :now " +
            "AND FUNCTION('TIMESTAMPADD', s.duration, s.startTime) >= :now")
    Optional<List<Session>> findActiveSessionsBySpot(
            @Param("spotId") Integer spotId,
            @Param("now") Instant now
    );

}
