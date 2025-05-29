package ua.deti.tqs.services;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import ua.deti.tqs.entities.*;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.ChargingSpotRepository;
import ua.deti.tqs.repositories.ChargingStationRepository;
import ua.deti.tqs.repositories.SessionRepository;
import ua.deti.tqs.repositories.VehicleRepository;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
  @Mock private SessionRepository sessionRepository;

  @Mock private ChargingSpotRepository chargingSpotRepository;
  @Mock private ChargingStationRepository chargingStationRepository;
  @Mock private VehicleRepository vehicleRepository;

  @InjectMocks private SessionServiceImpl sessionService;

  private Vehicle vehicle;
  private User user;
  private Session session;
  private ChargingSpot chargingSpot;
  private ChargingStation chargingStation;

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

    chargingStation = new ChargingStation();
    chargingStation.setId(1);
    chargingStation.setName("Charging Station 1");
    chargingStation.setLat(BigDecimal.valueOf(40.7128));
    chargingStation.setLon(BigDecimal.valueOf(-74.0060));

    chargingSpot = new ChargingSpot();
    chargingSpot.setId(1);
    chargingSpot.setStation(chargingStation);
    chargingSpot.setPowerKw(BigDecimal.TEN);
    chargingSpot.setPricePerKwh(BigDecimal.valueOf(1.5));

    session = new Session();
    session.setId(1);
    session.setVehicle(vehicle);
    session.setChargingSpot(chargingSpot);
    session.setStartTime(Instant.now().plusSeconds(3600));
    session.setDuration(3600);
  }

  @Test
  void whenGetAllSessionsByUserId_thenReturnSessions() {
    when(sessionRepository.findAllByVehicle_User_Id(user.getId()))
        .thenReturn(Optional.of(List.of(session)));

    List<Session> found = sessionService.getAllSessionsByUserId(user.getId());

    assertThat(found).isNotNull();
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getId()).isEqualTo(1);
  }

  @Test
  void whenGetAllSessionsByUserId_thenReturnEmptyList() {
    when(sessionRepository.findAllByVehicle_User_Id(user.getId())).thenReturn(Optional.empty());

    List<Session> found = sessionService.getAllSessionsByUserId(user.getId());

    assertThat(found).isNotNull();
    assertThat(found).isEmpty();
  }

  @Test
  void whenGetAllSessionsByStationId_thenReturnSessions() {
    when(chargingStationRepository.existsById(chargingStation.getId())).thenReturn(true);
    when(sessionRepository.findAllByChargingSpot_Station_Id(anyInt()))
        .thenReturn(Optional.of(List.of(session)));

    List<Session> found = sessionService.getAllSessionsByStationId(chargingStation.getId());

    assertThat(found).isNotNull();
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getId()).isEqualTo(1);
  }

  @Test
  void whenGetAllSessionsByStationId_thenReturnEmptyList() {
    when(chargingStationRepository.existsById(chargingStation.getId())).thenReturn(true);
    when(sessionRepository.findAllByChargingSpot_Station_Id(anyInt())).thenReturn(Optional.empty());

    List<Session> found = sessionService.getAllSessionsByStationId(chargingStation.getId());

    assertThat(found).isNotNull();
    assertThat(found).isEmpty();
  }

  @Test
  void whenGetAllSessionsByStationId_withInvalidStationId_thenReturnEmptyList() {
    when(chargingStationRepository.existsById(chargingStation.getId())).thenReturn(false);

    List<Session> found = sessionService.getAllSessionsByStationId(chargingStation.getId());

    assertThat(found).isNotNull();
    assertThat(found).isEmpty();
  }

  @Test
  void whenCreateSession_withInvalidVehicle_thenReturnNull() {
    when(vehicleRepository.findById(anyInt())).thenReturn(Optional.empty());

    Session created = sessionService.createSession(user.getId(), session);

    assertThat(created).isNull();
  }

  @Test
  void whenCreateSession_withVehicleBelongingToDifferentUser_thenReturnNull() {
    User differentUser = new User();
    differentUser.setId(2);
    vehicle.setUser(differentUser);

    when(vehicleRepository.findById(session.getVehicle().getId())).thenReturn(Optional.of(vehicle));

    Session created = sessionService.createSession(user.getId(), session);

    assertThat(created).isNull();
  }

  @Test
  void whenCreateSession_withInvalidChargingSpot_thenReturnNull() {
    when(vehicleRepository.findById(session.getVehicle().getId())).thenReturn(Optional.of(vehicle));
    when(chargingSpotRepository.findById(session.getChargingSpot().getId()))
        .thenReturn(Optional.empty());

    Session created = sessionService.createSession(user.getId(), session);

    assertThat(created).isNull();
  }

  @Test
  void whenCreateSession_withOverlappingSession_thenReturnNull() {
    Session overlappingSession = new Session();
    overlappingSession.setStartTime(session.getStartTime().plusSeconds(1800)); // 30 mins after
    overlappingSession.setDuration(3600); // 1 hour duration (will overlap)

    when(vehicleRepository.findById(session.getVehicle().getId())).thenReturn(Optional.of(vehicle));
    when(chargingSpotRepository.findById(session.getChargingSpot().getId()))
        .thenReturn(Optional.of(chargingSpot));
    when(sessionRepository.findAllByChargingSpot_Id(chargingSpot.getId()))
        .thenReturn(Optional.of(List.of(overlappingSession)));

    Session created = sessionService.createSession(user.getId(), session);

    assertThat(created).isNull();
  }

  @Test
  void whenCreateSession_withNullStartTime_thenReturnNull() {
    session.setStartTime(null);

    when(vehicleRepository.findById(session.getVehicle().getId())).thenReturn(Optional.of(vehicle));
    when(chargingSpotRepository.findById(session.getChargingSpot().getId()))
        .thenReturn(Optional.of(chargingSpot));
    when(sessionRepository.findAllByChargingSpot_Id(chargingSpot.getId()))
        .thenReturn(Optional.empty());

    Session created = sessionService.createSession(user.getId(), session);

    assertThat(created).isNull();
  }

  @Test
  void whenCreateSession_withInvalidDuration_thenReturnNull() {
    session.setDuration(0);

    when(vehicleRepository.findById(session.getVehicle().getId())).thenReturn(Optional.of(vehicle));
    when(chargingSpotRepository.findById(session.getChargingSpot().getId()))
        .thenReturn(Optional.of(chargingSpot));
    when(sessionRepository.findAllByChargingSpot_Id(chargingSpot.getId()))
        .thenReturn(Optional.empty());

    Session created = sessionService.createSession(user.getId(), session);

    assertThat(created).isNull();
  }

  @Test
  void whenCreateSession_withMultipleValidationErrors_thenReturnNull() {
    session.setStartTime(null);
    session.setDuration(0);

    when(vehicleRepository.findById(session.getVehicle().getId())).thenReturn(Optional.of(vehicle));
    when(chargingSpotRepository.findById(session.getChargingSpot().getId()))
        .thenReturn(Optional.of(chargingSpot));
    when(sessionRepository.findAllByChargingSpot_Id(chargingSpot.getId()))
        .thenReturn(Optional.empty());

    Session created = sessionService.createSession(user.getId(), session);

    assertThat(created).isNull();
  }

  @Test
  void whenCreateSession_withValidData_thenReturnSession() {
    when(vehicleRepository.findById(session.getVehicle().getId())).thenReturn(Optional.of(vehicle));
    when(chargingSpotRepository.findById(session.getChargingSpot().getId()))
        .thenReturn(Optional.of(chargingSpot));
    when(sessionRepository.findAllByChargingSpot_Id(chargingSpot.getId()))
        .thenReturn(Optional.empty());
    when(sessionRepository.save(any(Session.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Session created = sessionService.createSession(user.getId(), session);

    assertThat(created).isNotNull();
    assertThat(created.getVehicle()).isEqualTo(vehicle);
    assertThat(created.getChargingSpot()).isEqualTo(chargingSpot);
    assertThat(created.getStartTime()).isEqualTo(session.getStartTime());
    assertThat(created.getDuration()).isEqualTo(session.getDuration());
  }

  @Test
  void whenCreateSession_thenCalculateTotalCost() {
    session.setDuration(3600); // 1 hour = 3600 seconds
    BigDecimal expectedCost = chargingSpot.getPricePerKwh().multiply(BigDecimal.valueOf(3600));

    when(vehicleRepository.findById(session.getVehicle().getId())).thenReturn(Optional.of(vehicle));
    when(chargingSpotRepository.findById(session.getChargingSpot().getId()))
        .thenReturn(Optional.of(chargingSpot));
    when(sessionRepository.findAllByChargingSpot_Id(chargingSpot.getId()))
        .thenReturn(Optional.empty());
    when(sessionRepository.save(any(Session.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Session created = sessionService.createSession(user.getId(), session);

    assertThat(created.getTotalCost()).isEqualByComparingTo(expectedCost);
  }

  @Test
  void whenCreateSession_withNoOverlappingSessions_thenReturnSession() {
    Session nonOverlappingSession = new Session();
    nonOverlappingSession.setStartTime(session.getStartTime().minusSeconds(7200)); // 2 hours before
    nonOverlappingSession.setDuration(3600); // 1 hour duration (won't overlap)

    when(vehicleRepository.findById(session.getVehicle().getId())).thenReturn(Optional.of(vehicle));
    when(chargingSpotRepository.findById(session.getChargingSpot().getId()))
        .thenReturn(Optional.of(chargingSpot));
    when(sessionRepository.findAllByChargingSpot_Id(chargingSpot.getId()))
        .thenReturn(Optional.of(List.of(nonOverlappingSession)));
    when(sessionRepository.save(any(Session.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Session created = sessionService.createSession(user.getId(), session);

    assertThat(created).isNotNull();
  }

  @Test
  void whenDeleteSession_thenReturnTrue() {
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

    boolean deleted = sessionService.deleteSession(user.getId(), session.getId());

    assertThat(deleted).isTrue();
  }

  @Test
  void whenDeleteSession_withInvalidSession_thenReturnFalse() {
    when(sessionRepository.findById(session.getId())).thenReturn(Optional.empty());

    boolean deleted = sessionService.deleteSession(user.getId(), session.getId());

    assertThat(deleted).isFalse();
    verify(sessionRepository, never()).delete(any());
  }

  @Test
  void whenDeleteSession_withDifferentUser_thenReturnFalse() {
    User differentUser = new User();
    differentUser.setId(2);
    vehicle.setUser(differentUser);

    when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

    boolean deleted = sessionService.deleteSession(user.getId(), session.getId());

    assertThat(deleted).isFalse();
    verify(sessionRepository, never()).delete(any());
  }
}
