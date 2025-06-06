package ua.deti.tqs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.entities.types.SpotState;
import ua.deti.tqs.repositories.ChargingSpotRepository;
import ua.deti.tqs.repositories.ChargingStationRepository;
import ua.deti.tqs.entities.Vehicle;
import ua.deti.tqs.entities.Session;
import ua.deti.tqs.repositories.SessionRepository;
import ua.deti.tqs.repositories.UserRepository;

import java.util.Collections;
import static org.mockito.ArgumentMatchers.anyInt;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ChargingSpotServiceTest {

  @Mock private ChargingStationRepository chargingStationRepository;
  @Mock private ChargingSpotRepository chargingSpotRepository;
  @Mock private SessionRepository sessionRepository;
  @Mock private UserRepository userRepository;


  @InjectMocks private ChargingSpotServiceImpl chargingSpotService;

  private ChargingStation chargingStation1;
  private ChargingSpot chargingSpot;
  private User operator1;

  private User regularUser;
  private Session activeSession;
  private Vehicle vehicle;
  private Instant testNow;

  @BeforeEach
  void setUp() {
    testNow = Instant.now();

    chargingStation1 = new ChargingStation();
    chargingStation1.setId(1);
    chargingStation1.setName("Charging Station 1");
    chargingStation1.setLat(BigDecimal.valueOf(40.7128));
    chargingStation1.setLon(BigDecimal.valueOf(-74.0060));
    chargingStation1.setPhotoUrl("https://example.com/photo.jpg");

    operator1 = new User();
    operator1.setId(1);
    operator1.setRole(Role.OPERATOR);
    chargingStation1.setOperator(operator1);

    chargingSpot = new ChargingSpot();
    chargingSpot.setId(1);
    chargingSpot.setStation(chargingStation1);
    chargingSpot.setPowerKw(BigDecimal.valueOf(50));
    chargingSpot.setPricePerKwh(BigDecimal.valueOf(0.5));

    regularUser = new User();
    regularUser.setId(2);
    regularUser.setRole(Role.USER);

    vehicle = new Vehicle();
    vehicle.setUser(regularUser);

    // Create an active session (started 1 minute ago, lasts 2 minutes)
    activeSession = new Session();
    activeSession.setId(1);
    activeSession.setVehicle(vehicle);
    activeSession.setChargingSpot(chargingSpot);
    activeSession.setStartTime(testNow.minusSeconds(60));
    activeSession.setDuration(120);
  }

  @Test
  void whenGetAllChargingSpotsByStationId_thenReturnChargingSpots() {
    when(chargingSpotRepository.findAllByStation_Id(chargingStation1.getId()))
        .thenReturn(Optional.of(List.of(chargingSpot)));

    List<ChargingSpot> found =
        chargingSpotService.getAllChargingSpotsByStationId(chargingStation1.getId());

    assertThat(found).isNotEmpty();
    assertThat(found.getFirst().getId()).isEqualTo(chargingSpot.getId());
    assertThat(found.getFirst().getPowerKw()).isEqualTo(chargingSpot.getPowerKw());
    assertThat(found.getFirst().getPricePerKwh()).isEqualTo(chargingSpot.getPricePerKwh());

    verify(chargingSpotRepository).findAllByStation_Id(chargingStation1.getId());
  }

  @Test
  void whenGetAllChargingSpotsByStationId_withInvalidId_thenReturnEmptyList() {
    when(chargingSpotRepository.findAllByStation_Id(999)).thenReturn(Optional.empty());

    List<ChargingSpot> found = chargingSpotService.getAllChargingSpotsByStationId(999);

    assertThat(found).isEmpty();

    verify(chargingSpotRepository).findAllByStation_Id(999);
  }

  @Test
  void whenCreateChargingSpot_thenReturnCreatedChargingSpot() {
    when(chargingStationRepository.findById(chargingStation1.getId()))
        .thenReturn(Optional.of(chargingStation1));
    when(chargingSpotRepository.save(any(ChargingSpot.class))).thenReturn(chargingSpot);

    ChargingSpot created = chargingSpotService.createChargingSpot(operator1.getId(), chargingSpot);

    assertThat(created).isNotNull();
    assertThat(created.getId()).isEqualTo(chargingSpot.getId());
    assertThat(created.getPowerKw()).isEqualTo(chargingSpot.getPowerKw());
    assertThat(created.getPricePerKwh()).isEqualTo(chargingSpot.getPricePerKwh());

    verify(chargingStationRepository).findById(chargingStation1.getId());
    verify(chargingSpotRepository).save(any(ChargingSpot.class));
  }

  @Test
  void whenCreateChargingSpot_withPartialData_thenReturnCreatedChargingSpot() {
    when(chargingStationRepository.findById(chargingStation1.getId()))
        .thenReturn(Optional.of(chargingStation1));
    when(chargingSpotRepository.save(any(ChargingSpot.class))).thenReturn(chargingSpot);

    chargingSpot.setConnectorType(null);
    chargingSpot.setState(null);
    chargingSpot.setChargingVelocity(null);

    ChargingSpot created = chargingSpotService.createChargingSpot(operator1.getId(), chargingSpot);

    assertThat(created).isNotNull();
    assertThat(created.getId()).isEqualTo(chargingSpot.getId());
    assertThat(created.getPowerKw()).isEqualTo(chargingSpot.getPowerKw());
    assertThat(created.getPricePerKwh()).isEqualTo(chargingSpot.getPricePerKwh());

    verify(chargingStationRepository).findById(chargingStation1.getId());
    verify(chargingSpotRepository).save(any(ChargingSpot.class));
  }

  @Test
  void whenCreateChargingSpot_withIncompleteData_thenReturnNull() {
    when(chargingStationRepository.findById(chargingStation1.getId()))
        .thenReturn(Optional.of(chargingStation1));

    chargingSpot.setPowerKw(null);
    chargingSpot.setPricePerKwh(null);

    ChargingSpot created = chargingSpotService.createChargingSpot(operator1.getId(), chargingSpot);

    assertThat(created).isNull();

    verify(chargingStationRepository).findById(chargingStation1.getId());
  }

  @Test
  void whenCreateChargingSpot_withInvalidStation_thenReturnNull() {
    when(chargingStationRepository.findById(chargingStation1.getId())).thenReturn(Optional.empty());

    ChargingSpot created = chargingSpotService.createChargingSpot(operator1.getId(), chargingSpot);

    assertThat(created).isNull();

    verify(chargingStationRepository).findById(chargingStation1.getId());
  }

  @Test
  void whenCreateChargingSpot_withoutStation_thenReturnNull() {
    chargingSpot.setStation(null);

    ChargingSpot created = chargingSpotService.createChargingSpot(operator1.getId(), chargingSpot);

    assertThat(created).isNull();
  }

  @Test
  void whenCreateChargingSpot_withInvalidOperator_thenReturnNull() {
    when(chargingStationRepository.findById(chargingStation1.getId()))
        .thenReturn(Optional.of(chargingStation1));

    ChargingSpot created = chargingSpotService.createChargingSpot(999, chargingSpot);

    assertThat(created).isNull();

    verify(chargingStationRepository).findById(chargingStation1.getId());
  }

  @Test
  void whenUpdateChargingSpot_thenReturnUpdatedChargingSpot() {
    when(chargingSpotRepository.findById(chargingSpot.getId()))
        .thenReturn(Optional.of(chargingSpot));
    when(chargingSpotRepository.save(chargingSpot)).thenReturn(chargingSpot);

    ChargingSpot updated = chargingSpotService.updateChargingSpot(operator1.getId(), chargingSpot);

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(chargingSpot.getId());
    assertThat(updated.getPowerKw()).isEqualTo(chargingSpot.getPowerKw());
    assertThat(updated.getPricePerKwh()).isEqualTo(chargingSpot.getPricePerKwh());

    verify(chargingSpotRepository).findById(chargingSpot.getId());
    verify(chargingSpotRepository).save(chargingSpot);
  }

  @Test
  void whenUpdateChargingSpot_withPartialData_thenReturnUpdatedChargingSpot() {
    when(chargingSpotRepository.findById(chargingSpot.getId()))
        .thenReturn(Optional.of(chargingSpot));
    when(chargingSpotRepository.save(chargingSpot)).thenReturn(chargingSpot);

    ChargingSpot toUpdate = new ChargingSpot();
    toUpdate.setId(chargingSpot.getId());
    toUpdate.setStation(chargingStation1);
    toUpdate.setPowerKw(null);
    toUpdate.setPricePerKwh(null);
    toUpdate.setConnectorType(null);
    toUpdate.setState(null);
    toUpdate.setChargingVelocity(null);

    ChargingSpot updated = chargingSpotService.updateChargingSpot(operator1.getId(), toUpdate);

    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(chargingSpot.getId());
    assertThat(updated.getPowerKw()).isEqualTo(chargingSpot.getPowerKw());
    assertThat(updated.getPricePerKwh()).isEqualTo(chargingSpot.getPricePerKwh());

    verify(chargingSpotRepository).findById(chargingSpot.getId());
    verify(chargingSpotRepository).save(chargingSpot);
  }

  @Test
  void whenUpdateChargingSpot_withInvalidId_thenReturnNull() {
    when(chargingSpotRepository.findById(operator1.getId())).thenReturn(Optional.empty());

    ChargingSpot updated = chargingSpotService.updateChargingSpot(999, chargingSpot);

    assertThat(updated).isNull();

    verify(chargingSpotRepository).findById(operator1.getId());
  }

  @Test
  void whenUpdateChargingSpot_withInvalidOperator_thenReturnNull() {
    when(chargingSpotRepository.findById(chargingSpot.getId()))
        .thenReturn(Optional.of(chargingSpot));

    chargingSpot.setStation(new ChargingStation());
    chargingSpot.getStation().setOperator(new User());
    chargingSpot.getStation().getOperator().setId(999);

    ChargingSpot updated = chargingSpotService.updateChargingSpot(operator1.getId(), chargingSpot);

    assertThat(updated).isNull();

    verify(chargingSpotRepository).findById(chargingSpot.getId());
  }

  @Test
  void whenDeleteChargingSpot_thenReturnTrue() {
    when(chargingSpotRepository.findById(chargingSpot.getId()))
        .thenReturn(Optional.of(chargingSpot));

    boolean deleted =
        chargingSpotService.deleteChargingSpot(chargingSpot.getId(), operator1.getId());

    assertThat(deleted).isTrue();

    verify(chargingSpotRepository).findById(chargingSpot.getId());
    verify(chargingSpotRepository).delete(chargingSpot);
  }

  @Test
  void whenDeleteChargingSpot_withInvalidId_thenReturnFalse() {
    when(chargingSpotRepository.findById(999)).thenReturn(Optional.empty());

    boolean deleted = chargingSpotService.deleteChargingSpot(999, operator1.getId());

    assertThat(deleted).isFalse();

    verify(chargingSpotRepository).findById(999);
  }

  @Test
  void whenDeleteChargingSpot_withInvalidOperator_thenReturnFalse() {
    when(chargingSpotRepository.findById(chargingSpot.getId()))
        .thenReturn(Optional.of(chargingSpot));

    chargingSpot.setStation(new ChargingStation());
    chargingSpot.getStation().setOperator(new User());
    chargingSpot.getStation().getOperator().setId(999);

    boolean deleted =
        chargingSpotService.deleteChargingSpot(chargingSpot.getId(), operator1.getId());

    assertThat(deleted).isFalse();

    verify(chargingSpotRepository).findById(chargingSpot.getId());
  }

  @Test
  void whenUpdateChargingSpotStatus_ActiveSessionExistsButUserNotOwner_thenReturnFalse() {
    when(chargingSpotRepository.findById(anyInt())).thenReturn(Optional.of(chargingSpot));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(operator1));
    when(sessionRepository.findAllByChargingSpot_Id(anyInt()))
            .thenReturn(Optional.of(List.of(activeSession)));

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            chargingSpot.getId(), SpotState.FREE, operator1.getId()
    );
    assertThat(updated).isFalse();
  }

  @Test
  void whenUpdateChargingSpotStatus_ActiveSessionUserOwnerButUserRoleUserAndStatusOutOfService_thenReturnFalse() {
    when(chargingSpotRepository.findById(anyInt())).thenReturn(Optional.of(chargingSpot));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(regularUser));
    when(sessionRepository.findAllByChargingSpot_Id(anyInt()))
            .thenReturn(Optional.of(List.of(activeSession)));

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            chargingSpot.getId(), SpotState.OUT_OF_SERVICE, regularUser.getId()
    );
    assertThat(updated).isFalse();
  }

  @Test
  void whenUpdateChargingSpotStatus_ActiveSessionUserOwnerAndOperatorStatusOutOfService_thenReturnTrue() {
    vehicle.setUser(operator1);
    when(chargingSpotRepository.findById(anyInt())).thenReturn(Optional.of(chargingSpot));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(operator1));
    when(sessionRepository.findAllByChargingSpot_Id(anyInt()))
            .thenReturn(Optional.of(List.of(activeSession)));

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            chargingSpot.getId(), SpotState.OUT_OF_SERVICE, operator1.getId()
    );
    assertThat(updated).isTrue();
    assertThat(chargingSpot.getState()).isEqualTo(SpotState.OUT_OF_SERVICE);
  }

  @Test
  void whenUpdateChargingSpotStatus_ActiveSessionUserOwnerAndStatusNotOutOfService_thenReturnTrue() {
    when(chargingSpotRepository.findById(anyInt())).thenReturn(Optional.of(chargingSpot));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(regularUser));
    when(sessionRepository.findAllByChargingSpot_Id(anyInt()))
            .thenReturn(Optional.of(List.of(activeSession)));

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            chargingSpot.getId(), SpotState.OCCUPIED, regularUser.getId()
    );
    assertThat(updated).isTrue();
    assertThat(chargingSpot.getState()).isEqualTo(SpotState.OCCUPIED);
  }

  @Test
  void whenUpdateChargingSpotStatus_NoActiveSession_thenReturnTrue() {
    Session inactiveSession = new Session();
    inactiveSession.setId(2);
    inactiveSession.setVehicle(vehicle);
    inactiveSession.setChargingSpot(chargingSpot);
    inactiveSession.setStartTime(testNow.minusSeconds(120));
    inactiveSession.setDuration(60);

    when(chargingSpotRepository.findById(anyInt())).thenReturn(Optional.of(chargingSpot));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(operator1));
    when(sessionRepository.findAllByChargingSpot_Id(anyInt()))
            .thenReturn(Optional.of(List.of(inactiveSession)));

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            chargingSpot.getId(), SpotState.FREE, operator1.getId()
    );
    assertThat(updated).isTrue();
    assertThat(chargingSpot.getState()).isEqualTo(SpotState.FREE);
  }

  @Test
  void whenUpdateChargingSpotStatus_OutOfServiceAdmin_thenReturnTrue() {
    chargingSpot.setState(SpotState.OUT_OF_SERVICE);
    when(chargingSpotRepository.findById(anyInt())).thenReturn(Optional.of(chargingSpot));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(operator1));
    when(sessionRepository.findAllByChargingSpot_Id(anyInt()))
            .thenReturn(Optional.of(Collections.emptyList()));

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            chargingSpot.getId(), SpotState.FREE, operator1.getId()
    );
    assertThat(updated).isTrue();
    assertThat(chargingSpot.getState()).isEqualTo(SpotState.FREE);
  }

  @Test
  void whenUpdateChargingSpotStatus_OutOfServiceAndUserNotOperator_thenReturnFalse() {
    chargingSpot.setState(SpotState.OUT_OF_SERVICE);
    when(chargingSpotRepository.findById(anyInt()))
            .thenReturn(Optional.of(chargingSpot));
    when(userRepository.findById(anyInt()))
            .thenReturn(Optional.of(regularUser));
    when(sessionRepository.findAllByChargingSpot_Id(anyInt()))
            .thenReturn(Optional.of(Collections.emptyList()));

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            chargingSpot.getId(), SpotState.OUT_OF_SERVICE, regularUser.getId()
    );
    assertThat(updated).isFalse();
    assertThat(chargingSpot.getState()).isEqualTo(SpotState.OUT_OF_SERVICE);
  }



  @Test
  void whenUpdateChargingSpotStatus_MultipleSessionsOnlyOneActive_thenCheckActive() {
    Session inactiveSession = new Session();
    inactiveSession.setId(2);
    inactiveSession.setVehicle(vehicle);
    inactiveSession.setChargingSpot(chargingSpot);
    inactiveSession.setStartTime(testNow.minusSeconds(120));
    inactiveSession.setDuration(60);

    when(chargingSpotRepository.findById(anyInt())).thenReturn(Optional.of(chargingSpot));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(regularUser));
    when(sessionRepository.findAllByChargingSpot_Id(anyInt()))
            .thenReturn(Optional.of(List.of(activeSession, inactiveSession)));

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            chargingSpot.getId(), SpotState.OCCUPIED, regularUser.getId()
    );
    assertThat(updated).isTrue();
    assertThat(chargingSpot.getState()).isEqualTo(SpotState.OCCUPIED);
  }
  @Test
  void whenUpdateChargingSpotStatus_ChargingSpotNotFound_thenReturnFalse() {
    when(chargingSpotRepository.findById(anyInt())).thenReturn(Optional.empty());

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            999, SpotState.FREE, operator1.getId()
    );

    assertThat(updated).isFalse();
    verify(chargingSpotRepository).findById(999);
  }

  @Test
  void whenUpdateChargingSpotStatus_UserNotFound_thenReturnFalse() {
    when(chargingSpotRepository.findById(anyInt())).thenReturn(Optional.of(chargingSpot));
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            chargingSpot.getId(), SpotState.FREE, 999
    );

    assertThat(updated).isFalse();
    verify(userRepository).findById(999);
  }

  @Test
  void whenUpdateChargingSpotStatus_StatusNull_thenReturnFalse() {
    when(chargingSpotRepository.findById(anyInt())).thenReturn(Optional.of(chargingSpot));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(operator1));

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            chargingSpot.getId(), null, operator1.getId()
    );

    assertThat(updated).isFalse();
  }

  @Test
  void whenUpdateChargingSpotStatus_AllConditionsMet_thenProceedBeyondNullCheck() {
    when(chargingSpotRepository.findById(anyInt())).thenReturn(Optional.of(chargingSpot));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(operator1));
    when(sessionRepository.findAllByChargingSpot_Id(anyInt()))
            .thenReturn(Optional.of(Collections.emptyList()));

    boolean updated = chargingSpotService.updateChargingSpotStatus(
            chargingSpot.getId(), SpotState.FREE, operator1.getId()
    );

    assertThat(updated).isTrue();
    assertThat(chargingSpot.getState()).isEqualTo(SpotState.FREE);
  }
}
