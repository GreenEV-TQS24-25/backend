package ua.deti.tqs.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.Vehicle;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.VehicleRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {
    @Mock private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleServiceImpl chargingStationService;

    private Vehicle vehicle;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setRole(Role.OPERATOR);

        vehicle = new Vehicle();
        vehicle.setId(1);
        vehicle.setBrand("Brand");
        vehicle.setModel("Model");
        vehicle.setLicensePlate("ABC123");
        vehicle.setUser(user);
    }

    @Test
    void whenGetAllVehiclesByUserId_thenReturnVehicles() {
        // given
        when(vehicleRepository.findAllByUser_Id(user.getId())).thenReturn(Optional.of(List.of(vehicle)));

        // when
        List<Vehicle> found = chargingStationService.getAllVehiclesByUserId(user.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getId()).isEqualTo(1);
    }

    @Test
    void whenGetAllVehiclesByUserId_thenReturnEmptyList() {
        // given
        when(vehicleRepository.findAllByUser_Id(user.getId())).thenReturn(Optional.empty());

        // when
        List<Vehicle> found = chargingStationService.getAllVehiclesByUserId(user.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found).isEmpty();
    }

    @Test
    void whenGetAllVehiclesByUserId_thenReturnNull() {
        // when
        List<Vehicle> found = chargingStationService.getAllVehiclesByUserId(user.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found).isEmpty();
    }

    @Test
    void whenCreateVehicle_thenReturnVehicle() {
        // given
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // when
        Vehicle created = chargingStationService.createVehicle(vehicle, user);

        // then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1);
    }

    @Test
    void whenCreateVehicle_withNullConnector_thenReturnVehicle() {
        // given
        vehicle.setConnectorType(null);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // when
        Vehicle created = chargingStationService.createVehicle(vehicle, user);

        // then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1);
    }

    @Test
    void whenCreateVehicle_withInvalidUser_thenReturnNull() {
        // when
        Vehicle created = chargingStationService.createVehicle(vehicle, user);

        // then
        assertThat(created).isNull();
    }

    @Test
    void whenCreateVehicle_withInvalidData_thenReturnNull() {
        // given
        vehicle.setBrand(null);
        vehicle.setModel(null);
        vehicle.setLicensePlate(null);
        vehicle.setConnectorType(null);

        // when
        Vehicle created = chargingStationService.createVehicle(vehicle, user);

        // then
        assertThat(created).isNull();
    }

    @Test
    void whenCreateVehicle_withEmptyData_thenReturnNull() {
        // given
        vehicle.setBrand("");
        vehicle.setModel("");
        vehicle.setLicensePlate("");

        // when
        Vehicle created = chargingStationService.createVehicle(vehicle, user);

        // then
        assertThat(created).isNull();
    }

    @Test
    void whenUpdateVehicle_thenReturnUpdatedVehicle() {
        // given
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        // when
        Vehicle updated = chargingStationService.updateVehicle(user.getId(), vehicle);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(1);
    }

    @Test
    void whenUpdateVehicle_withWrongId_thenReturnNull() {
        // given
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

        // when
        Vehicle updated = chargingStationService.updateVehicle(4, vehicle);

        // then
        assertThat(updated).isNull();
    }

    @Test
    void whenUpdateVehicle_withInvalidUser_thenReturnNull() {
        // when
        Vehicle updated = chargingStationService.updateVehicle(user.getId(), vehicle);

        // then
        assertThat(updated).isNull();
    }

    @Test
    void whenUpdateVehicle_withInvalidData_thenReturnNull() {
        // given
        vehicle.setBrand(null);
        vehicle.setModel(null);
        vehicle.setLicensePlate(null);
        vehicle.setConnectorType(null);


        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

        // when
        Vehicle updated = chargingStationService.updateVehicle(user.getId(), vehicle);

        // then
        assertThat(updated).isNull();
    }

    @Test
    void whenUpdateVehicle_withEmptyData_thenReturnNull() {
        // given
        vehicle.setBrand("");
        vehicle.setModel("");
        vehicle.setLicensePlate("");


        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

        // when
        Vehicle updated = chargingStationService.updateVehicle(user.getId(), vehicle);

        // then
        assertThat(updated).isNull();
    }

    @Test
    void whenDeleteVehicle_thenReturnTrue() {
        // given
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

        // when
        boolean deleted = chargingStationService.deleteVehicle(user.getId(), vehicle.getId());

        // then
        assertThat(deleted).isTrue();
    }

    @Test
    void whenDeleteVehicle_withInvalidUser_thenReturnFalse() {
        // when
        boolean deleted = chargingStationService.deleteVehicle(user.getId(), vehicle.getId());

        // then
        assertThat(deleted).isFalse();
    }

    @Test
    void whenDeleteVehicle_withInvalidVehicle_thenReturnFalse() {
        // given
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.empty());

        // when
        boolean deleted = chargingStationService.deleteVehicle(user.getId(), vehicle.getId());

        // then
        assertThat(deleted).isFalse();
    }

    @Test
    void whenDeleteVehicle_withInvalidUserId_thenReturnFalse() {
        // given
        when(vehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));

        // when
        boolean deleted = chargingStationService.deleteVehicle(vehicle.getId(),2);

        // then
        assertThat(deleted).isFalse();
    }
}
