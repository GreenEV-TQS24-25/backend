package ua.deti.tqs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import ua.deti.tqs.repositories.ChargingSpotRepository;
import ua.deti.tqs.repositories.ChargingStationRepository;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ChargingSpotServiceTest {

  @Mock private ChargingStationRepository chargingStationRepository;

  @Mock private ChargingSpotRepository chargingSpotRepository;

  @InjectMocks private ChargingSpotServiceImpl chargingSpotService;

  private ChargingStation chargingStation1;

  private ChargingSpot chargingSpot;

  private User operator1;

  @BeforeEach
  void setUp() {
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
}
