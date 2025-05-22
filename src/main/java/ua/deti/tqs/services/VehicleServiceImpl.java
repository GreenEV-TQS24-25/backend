package ua.deti.tqs.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.Vehicle;
import ua.deti.tqs.repositories.VehicleRepository;
import ua.deti.tqs.services.interfaces.VehicleService;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class VehicleServiceImpl implements VehicleService {
     private final VehicleRepository vehicleRepository;
    @Override
    public List<Vehicle> getAllVehiclesByUserId(int userId) {
        log.debug("Fetching all vehicles with user id {}", userId);
        List<Vehicle> vehicles = vehicleRepository
                .findAllByUser_Id(userId)
                .orElse(null);

        if (vehicles == null) {
            log.debug("No vehicles found with user id {}", userId);
            return List.of();
        }

        log.debug("Found {} vehicles with user id {}", vehicles.size(), userId);
        return vehicles;
    }

    @Override
    public Vehicle createVehicle(Vehicle vehicle, User user) {
        log.debug("Creating new vehicle {}", vehicle);

        int errorCount = 0;
        if (vehicle.getBrand() == null || vehicle.getBrand().isEmpty()) {
            log.debug("Invalid vehicle brand, brand is null or empty");
            errorCount++;
        }
        if (vehicle.getModel() == null || vehicle.getModel().isEmpty()) {
            log.debug("Invalid vehicle model, model is null or empty");
            errorCount++;
        }
        if (vehicle.getLicensePlate() == null || vehicle.getLicensePlate().isEmpty()) {
            log.debug("Invalid vehicle license plate, license plate is null or empty");
            errorCount++;
        }

        if (errorCount > 0) return null;


        Vehicle newVehicle = new Vehicle();
        newVehicle.setUser(user);
        newVehicle.setBrand(vehicle.getBrand());
        newVehicle.setModel(vehicle.getModel());
        newVehicle.setLicensePlate(vehicle.getLicensePlate());

        if (vehicle.getConnectorType() != null)
            newVehicle.setConnectorType(vehicle.getConnectorType());

        log.debug("Saving new vehicle {}", newVehicle);
        return vehicleRepository.save(newVehicle);
    }

    @Override
    public Vehicle updateVehicle(int userId, Vehicle vehicle) {
        log.debug("Updating vehicle with id {} and user id {}", vehicle.getId(), userId);
        Vehicle existingVehicle = vehicleRepository.findById(vehicle.getId()).orElse(null);

        if (existingVehicle == null) {
            log.debug("Vehicle not found");
            return null;
        }

        if (existingVehicle.getUser().getId() != userId) {
            log.debug("Vehicle with id {} does not belong to user with id {}", vehicle.getId(), userId);
            return null;
        }

        if (vehicle.getBrand() != null && !vehicle.getBrand().isEmpty()) {
            existingVehicle.setBrand(vehicle.getBrand());
        }
        if (vehicle.getModel() != null && !vehicle.getModel().isEmpty()) {
            existingVehicle.setModel(vehicle.getModel());
        }
        if (vehicle.getLicensePlate() != null && !vehicle.getLicensePlate().isEmpty()) {
            existingVehicle.setLicensePlate(vehicle.getLicensePlate());
        }
        if (vehicle.getConnectorType() != null) {
            existingVehicle.setConnectorType(vehicle.getConnectorType());
        }

        log.debug("Saving updated vehicle {}", existingVehicle);
        return vehicleRepository.save(existingVehicle);
    }

    @Override
    public boolean deleteVehicle(int id, int userId) {
        log.debug("Deleting vehicle with id {} and user id {}", id, userId);
        Vehicle vehicle = vehicleRepository.findById(id).orElse(null);

        if (vehicle == null) {
            log.debug("No vehicle found with id {} and user id {}", id, userId);
            return false;
        }

        if (vehicle.getUser().getId() != userId) {
            log.debug("The user with id {}, is not the owner", userId);
            return false;
        }

        vehicleRepository.delete(vehicle);
        log.debug("Deleted vehicle with id {} and user id {}", id, userId);
        return true;
    }
}