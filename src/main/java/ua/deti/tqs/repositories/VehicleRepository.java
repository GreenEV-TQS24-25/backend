package ua.deti.tqs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.deti.tqs.entities.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    Optional<List<Vehicle>> findAllByUser_Id(int userId);

}