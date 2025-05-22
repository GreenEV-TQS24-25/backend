package ua.deti.tqs.services.interfaces;

import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.Vehicle;

import java.util.List;

public interface VehicleService {
    List<Vehicle> getAllVehiclesByUserId(int userId);

    Vehicle createVehicle(Vehicle vehicle, User user);

    Vehicle updateVehicle(int userId, Vehicle vehicle);

    boolean deleteVehicle(int id, int userId);
}
