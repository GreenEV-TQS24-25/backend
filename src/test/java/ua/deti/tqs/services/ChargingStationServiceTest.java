package ua.deti.tqs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import ua.deti.tqs.dto.StationsSpots;
import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.types.ConnectorType;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.ChargingSpotRepository;
import ua.deti.tqs.repositories.ChargingStationRepository;
import ua.deti.tqs.repositories.UserRepository;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ChargingStationServiceTest {

  @Mock private ChargingStationRepository chargingStationRepository;

  @Mock private UserRepository userRepository;

  @Mock private ChargingSpotRepository chargingSpotRepository;

  @InjectMocks private ChargingStationServiceImpl chargingStationService;

  private ChargingStation chargingStation1;
  private ChargingStation chargingStation2;
  private ChargingStation chargingStation3;

  private User operator1;

  @BeforeEach
  void setUp() {
    chargingStation1 = new ChargingStation();
    chargingStation1.setId(1);
    chargingStation1.setName("Charging Station 1");
    chargingStation1.setLat(BigDecimal.valueOf(40.7128));
    chargingStation1.setLon(BigDecimal.valueOf(-74.0060));
    chargingStation1.setPhotoUrl("https://example.com/photo.jpg");

    chargingStation2 = new ChargingStation();
    chargingStation2.setId(2);
    chargingStation2.setName("Charging Station 2");
    chargingStation2.setLat(BigDecimal.valueOf(50.7128));
    chargingStation2.setLon(BigDecimal.valueOf(-64.0060));

    chargingStation3 = new ChargingStation();
    chargingStation3.setId(3);
    chargingStation3.setName("Charging Station 3");
    chargingStation3.setLat(BigDecimal.valueOf(60.7128));
    chargingStation3.setLon(BigDecimal.valueOf(-54.0060));

    operator1 = new User();
    operator1.setId(1);
    operator1.setRole(Role.OPERATOR);
    chargingStation1.setOperator(operator1);
    chargingStation2.setOperator(operator1);
    chargingStation3.setOperator(operator1);
  }

  @Test
  void getAllChargingStationsByOperatorId_operatorValid_stationsFound() {
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    when(chargingStationRepository.findAllByOperator_Id(1))
            .thenReturn(Optional.of(List.of(chargingStation1)));
    when(chargingSpotRepository.findAllByStation_Id(1))
            .thenReturn(Optional.of(Collections.emptyList()));

    List<StationsSpots> result = chargingStationService.getAllChargingStationsByOperatorId(1);

    assertThat(result).isNotEmpty();
    assertThat(result.get(0).getChargingStation()).isEqualTo(chargingStation1);
    assertThat(result.get(0).getSpots()).isEmpty();
  }

  @Test
  void getAllChargingStationsByOperatorId_operatorValid_noStationsFound() {
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    when(chargingStationRepository.findAllByOperator_Id(1)).thenReturn(Optional.empty());

    List<StationsSpots> result = chargingStationService.getAllChargingStationsByOperatorId(1);

    assertThat(result).isEmpty();
  }

  @Test
  void whenGetAllChargingStationsByOperatorId_withUserNotOperator_thenReturnEmptyList() {
    operator1.setRole(Role.USER);
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));

    List<StationsSpots> result = chargingStationService.getAllChargingStationsByOperatorId(1);

    assertThat(result).isEmpty();
  }

  @Test
  void whenCreateChargingStation_withOperatorIdMismatch_thenReturnNull() {
    User differentOperator = new User();
    differentOperator.setId(2);
    differentOperator.setRole(Role.OPERATOR);
    chargingStation1.setOperator(differentOperator);
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    ChargingStation result = chargingStationService.createChargingStation(chargingStation1, 1);

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withOperatorIdCorrect_andChargingStationIdIncorrect() {
    User differentOperator = new User();
    differentOperator.setId(2);
    differentOperator.setRole(Role.OPERATOR);
    chargingStation1.setOperator(differentOperator);
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    when(userRepository.findById(2)).thenReturn(Optional.of(differentOperator));

    ChargingStation result = chargingStationService.createChargingStation(chargingStation1, 1);

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withOperatorBeingNull_thenReturnNull() {
    chargingStation1.setOperator(null);
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));

    ChargingStation result = chargingStationService.createChargingStation(chargingStation1, 1);

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withLatNull_thenReturnNull() {
    chargingStation1.setLat(null);
    chargingStation1.setLon(BigDecimal.valueOf(-74.0060));
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));

    ChargingStation result = chargingStationService.createChargingStation(chargingStation1, 1);

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withLonNull_thenReturnNull() {
    chargingStation1.setLat(BigDecimal.valueOf(40.7128));
    chargingStation1.setLon(null);
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));

    ChargingStation result = chargingStationService.createChargingStation(chargingStation1, 1);

    assertThat(result).isNull();
  }

  @Test
  void whenUpdateChargingStation_withChangedOperator_thenIgnoreChange() {
    User newOperator = new User();
    newOperator.setId(2);
    chargingStation1.setOperator(newOperator);
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    when(chargingStationRepository.findById(1)).thenReturn(Optional.of(chargingStation1));

    ChargingStation result = chargingStationService.updateChargingStation(1, chargingStation1);

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withNonMatchingOperatorId_thenReturnNull() {
    User differentOperator = new User();
    differentOperator.setId(2);
    differentOperator.setRole(Role.OPERATOR);

    ChargingStation stationWithDifferentOperator = new ChargingStation();
    stationWithDifferentOperator.setName("Test Station");
    stationWithDifferentOperator.setLat(BigDecimal.valueOf(40.7128));
    stationWithDifferentOperator.setLon(BigDecimal.valueOf(-74.0060));
    stationWithDifferentOperator.setOperator(differentOperator);

    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));

    ChargingStation result =
            chargingStationService.createChargingStation(stationWithDifferentOperator, 1);

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withNullStation_thenReturnNull() {
    ChargingStation result = chargingStationService.createChargingStation(null, operator1.getId());

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withNullName_thenReturnNull() {
    ChargingStation invalidStation = new ChargingStation();
    invalidStation.setName(null);
    invalidStation.setLat(BigDecimal.valueOf(40.7128));
    invalidStation.setLon(BigDecimal.valueOf(-74.0060));
    invalidStation.setOperator(operator1);

    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));

    ChargingStation result =
            chargingStationService.createChargingStation(invalidStation, operator1.getId());

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withInvalidLatitude_thenReturnNull() {
    ChargingStation invalidStation = new ChargingStation();
    invalidStation.setName("Invalid Station");
    invalidStation.setLat(
            BigDecimal.valueOf(100));
    invalidStation.setLon(BigDecimal.valueOf(-74.0060));
    invalidStation.setOperator(operator1);

    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));

    ChargingStation result =
            chargingStationService.createChargingStation(invalidStation, operator1.getId());

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withInvalidLongitude_thenReturnNull() {
    ChargingStation invalidStation = new ChargingStation();
    invalidStation.setName("Invalid Station");
    invalidStation.setLat(BigDecimal.valueOf(40.7128));
    invalidStation.setLon(
            BigDecimal.valueOf(190));
    invalidStation.setOperator(operator1);

    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));

    ChargingStation result =
            chargingStationService.createChargingStation(invalidStation, operator1.getId());

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withEmptyName_thenReturnNull() {
    ChargingStation invalidStation = new ChargingStation();
    invalidStation.setName("");
    invalidStation.setLat(BigDecimal.valueOf(40.7128));
    invalidStation.setLon(BigDecimal.valueOf(-74.0060));
    invalidStation.setOperator(operator1);

    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));

    ChargingStation result =
            chargingStationService.createChargingStation(invalidStation, operator1.getId());

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withOperatorRoleNotOperator_thenReturnNull() {
    User nonOperatorUser = new User();
    nonOperatorUser.setId(1);
    nonOperatorUser.setRole(Role.USER);

    ChargingStation invalidStation = new ChargingStation();
    invalidStation.setName("Invalid Station");
    invalidStation.setLat(BigDecimal.valueOf(40.7128));
    invalidStation.setLon(BigDecimal.valueOf(-74.0060));
    invalidStation.setOperator(nonOperatorUser);

    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));

    ChargingStation result = chargingStationService.createChargingStation(invalidStation, 1);

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withDifferentOperatorId_thenReturnNull() {
    User differentOperator = new User();
    differentOperator.setId(2);
    differentOperator.setRole(Role.OPERATOR);

    ChargingStation invalidStation = new ChargingStation();
    invalidStation.setName("Invalid Station");
    invalidStation.setLat(BigDecimal.valueOf(40.7128));
    invalidStation.setLon(BigDecimal.valueOf(-74.0060));
    invalidStation.setOperator(differentOperator);

    when(userRepository.findById(1))
            .thenReturn(Optional.of(operator1));

    ChargingStation result = chargingStationService.createChargingStation(invalidStation, 1);

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withInvalidOperatorIdZero_thenReturnNull() {
    User invalidOperator = new User();
    invalidOperator.setId(0);
    invalidOperator.setRole(Role.OPERATOR);

    ChargingStation invalidStation = new ChargingStation();
    invalidStation.setName("Invalid Station");
    invalidStation.setLat(BigDecimal.valueOf(40.7128));
    invalidStation.setLon(BigDecimal.valueOf(-74.0060));
    invalidStation.setOperator(invalidOperator);

    when(userRepository.findById(1))
            .thenReturn(Optional.of(operator1));

    ChargingStation result = chargingStationService.createChargingStation(invalidStation, 1);

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withInvalidOperatorIdNegative_thenReturnNull() {
    User invalidOperator = new User();
    invalidOperator.setId(-1);
    invalidOperator.setRole(Role.OPERATOR);

    ChargingStation invalidStation = new ChargingStation();
    invalidStation.setName("Invalid Station");
    invalidStation.setLat(BigDecimal.valueOf(40.7128));
    invalidStation.setLon(BigDecimal.valueOf(-74.0060));
    invalidStation.setOperator(invalidOperator);

    when(userRepository.findById(1))
            .thenReturn(Optional.of(operator1));

    ChargingStation result = chargingStationService.createChargingStation(invalidStation, 1);

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withValidOperatorButDifferentAuthenticatedId_thenReturnNull() {
    ChargingStation validStation = new ChargingStation();
    validStation.setName("Valid Station");
    validStation.setLat(BigDecimal.valueOf(40.7128));
    validStation.setLon(BigDecimal.valueOf(-74.0060));
    validStation.setOperator(operator1);

    when(userRepository.findById(3))
            .thenReturn(Optional.of(operator1));

    ChargingStation result = chargingStationService.createChargingStation(validStation, 3);

    assertThat(result).isNull();
  }

  @Test
  void whenGetAllChargingStationsByOperatorId_thenReturnListWithSpots() {
    List<ChargingStation> chargingStations = List.of(chargingStation1);
    List<ChargingSpot> spots = List.of(new ChargingSpot());

    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    when(chargingStationRepository.findAllByOperator_Id(1))
            .thenReturn(Optional.of(chargingStations));
    when(chargingSpotRepository.findAllByStation_Id(1))
            .thenReturn(Optional.of(spots));

    List<StationsSpots> result = chargingStationService.getAllChargingStationsByOperatorId(1);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getChargingStation()).isEqualTo(chargingStation1);
    assertThat(result.get(0).getSpots()).isEqualTo(spots);
    verify(chargingStationRepository).findAllByOperator_Id(1);
  }

  @Test
  void whenGetAllChargingStationsByOperatorId_withNoStationsFound_thenReturnEmptyList() {
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    when(chargingStationRepository.findAllByOperator_Id(1)).thenReturn(Optional.empty());

    List<StationsSpots> result = chargingStationService.getAllChargingStationsByOperatorId(1);

    assertThat(result).isEmpty();
    verify(chargingStationRepository).findAllByOperator_Id(1);
  }

  @Test
  void whenGetAllChargingStationsByOperatorId_withInvalidOperatorId_thenReturnEmptyList() {
    when(userRepository.findById(999)).thenReturn(Optional.empty());

    List<StationsSpots> result = chargingStationService.getAllChargingStationsByOperatorId(999);

    assertThat(result).isEmpty();
  }

  @Test
  void whenGetAllChargingStations_thenReturnList() {
    ChargingSpot spot = new ChargingSpot();
    spot.setId(1);
    spot.setStation(chargingStation1);

    when(chargingStationRepository.findAll()).thenReturn(List.of(chargingStation1));
    when(chargingSpotRepository.findAllByStation_Id(1))
            .thenReturn(Optional.of(List.of(spot)));

    List<StationsSpots> result = chargingStationService.getAllChargingStations();

    assertThat(result).hasSize(1);
    StationsSpots stationsSpots = result.get(0);
    assertThat(stationsSpots.getChargingStation().getId()).isEqualTo(chargingStation1.getId());
    assertThat(stationsSpots.getSpots()).hasSize(1);
  }

  @Test
  void whenGetAllChargingStations_withEmptyList_thenReturnEmptyList() {
    when(chargingStationRepository.findAll()).thenReturn(List.of());

    List<StationsSpots> result = chargingStationService.getAllChargingStations();

    assertThat(result).isEmpty();
    verify(chargingSpotRepository, never()).findAllByStation_Id(anyInt());
  }

  @Test
  void whenCreateChargingStation_thenReturnChargingStation() {
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    when(chargingStationRepository.save(any(ChargingStation.class))).thenReturn(chargingStation1);

    ChargingStation result =
            chargingStationService.createChargingStation(chargingStation1, operator1.getId());

    assertThat(result).isEqualTo(chargingStation1);
  }

  @Test
  void whenCreateChargingStation_withTheBasicInfo_thenReturnChargingStation() {
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    chargingStation1.setPhotoUrl(null);

    when(chargingStationRepository.save(any(ChargingStation.class))).thenReturn(chargingStation1);

    ChargingStation result =
            chargingStationService.createChargingStation(chargingStation1, operator1.getId());

    assertThat(result).isEqualTo(chargingStation1);
  }

  @Test
  void whenCreateChargingStation_withInvalidUser_thenReturnNull() {
    when(userRepository.findById(999)).thenReturn(Optional.empty());

    ChargingStation result = chargingStationService.createChargingStation(chargingStation1, 999);
    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withInvalidData_thenReturnNull() {
    ChargingStation invalidChargingStation = new ChargingStation();
    invalidChargingStation.setName(null);
    invalidChargingStation.setLat(null);
    invalidChargingStation.setLon(null);

    ChargingStation result =
            chargingStationService.createChargingStation(invalidChargingStation, operator1.getId());

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withPartialInvalidData_thenReturnNull() {
    ChargingStation invalidChargingStation = new ChargingStation();
    invalidChargingStation.setName("");
    invalidChargingStation.setLat(BigDecimal.valueOf(100.0));
    invalidChargingStation.setLon(null);

    ChargingStation result =
            chargingStationService.createChargingStation(invalidChargingStation, operator1.getId());

    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withNullOperator_thenReturnNull() {
    ChargingStation invalidChargingStation = new ChargingStation();
    invalidChargingStation.setName("Invalid Station");
    invalidChargingStation.setLat(BigDecimal.valueOf(40.7128));
    invalidChargingStation.setLon(BigDecimal.valueOf(-74.0060));
    invalidChargingStation.setOperator(null);

    ChargingStation result =
            chargingStationService.createChargingStation(invalidChargingStation, operator1.getId());
    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withAUserNotOperator_thenReturnNull() {
    ChargingStation invalidChargingStation = new ChargingStation();
    invalidChargingStation.setName("Invalid Station");
    invalidChargingStation.setLat(BigDecimal.valueOf(40.7128));
    invalidChargingStation.setLon(BigDecimal.valueOf(-74.0060));

    operator1.setRole(Role.USER);
    invalidChargingStation.setOperator(operator1);

    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));

    ChargingStation result =
            chargingStationService.createChargingStation(invalidChargingStation, operator1.getId());
    assertThat(result).isNull();
  }

  @Test
  void whenCreateChargingStation_withOperatorNotFound_thenReturnNull() {
    ChargingStation invalidChargingStation = new ChargingStation();
    invalidChargingStation.setName("Invalid Station");
    invalidChargingStation.setLat(BigDecimal.valueOf(40.7128));
    invalidChargingStation.setLon(BigDecimal.valueOf(-74.0060));

    invalidChargingStation.setOperator(operator1);

    when(userRepository.findById(1)).thenReturn(Optional.empty());

    ChargingStation result =
            chargingStationService.createChargingStation(invalidChargingStation, operator1.getId());
    assertThat(result).isNull();
  }

  @Test
  void whenUpdateChargingStation_thenReturnUpdatedChargingStation() {
    ChargingStation updatedChargingStation = new ChargingStation();
    updatedChargingStation.setName("Updated Station");
    updatedChargingStation.setId(1);
    updatedChargingStation.setLat(BigDecimal.valueOf(40.7128));
    updatedChargingStation.setLon(BigDecimal.valueOf(-74.0060));
    updatedChargingStation.setPhotoUrl("https://example.com/updated_photo.jpg");
    updatedChargingStation.setOperator(operator1);
    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    when(chargingStationRepository.findById(1)).thenReturn(Optional.of(chargingStation1));
    when(chargingStationRepository.save(any(ChargingStation.class)))
            .thenReturn(updatedChargingStation);

    ChargingStation result =
            chargingStationService.updateChargingStation(1, updatedChargingStation);
    assertThat(result).isEqualTo(updatedChargingStation);
  }

  @Test
  void whenUpdateChargingStation_withInvalidOperatorId_thenReturnNull() {

    ChargingStation result = chargingStationService.updateChargingStation(999, chargingStation1);

    assertThat(result).isNull();
  }

  @Test
  void whenUpdateChargingStation_withNonExistentId_thenReturnNull() {
    ChargingStation stationToUpdate = new ChargingStation();
    stationToUpdate.setId(999);
    stationToUpdate.setName("Non-existent Station");
    stationToUpdate.setLat(BigDecimal.valueOf(40.7128));
    stationToUpdate.setLon(BigDecimal.valueOf(-74.0060));
    stationToUpdate.setOperator(operator1);

    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    when(chargingStationRepository.findById(999)).thenReturn(Optional.empty());

    ChargingStation result = chargingStationService.updateChargingStation(1, stationToUpdate);

    assertThat(result).isNull();
    verify(chargingStationRepository).findById(999);
    verify(chargingStationRepository, never()).save(any(ChargingStation.class));
  }

  @Test
  void whenUpdateChargingStationWithIncompleteData_thenReturnUpdatedChargingStation() {
    ChargingStation updatedChargingStation = new ChargingStation();
    updatedChargingStation.setId(1);
    updatedChargingStation.setName("");
    updatedChargingStation.setLat(null);
    updatedChargingStation.setLon(null);
    updatedChargingStation.setPhotoUrl(null);
    updatedChargingStation.setOperator(operator1);

    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    when(chargingStationRepository.findById(1)).thenReturn(Optional.of(chargingStation1));
    when(chargingStationRepository.save(any(ChargingStation.class)))
            .thenReturn(updatedChargingStation);

    ChargingStation result =
            chargingStationService.updateChargingStation(1, updatedChargingStation);

    assertThat(result).isEqualTo(updatedChargingStation);

    updatedChargingStation.setName(null);
    result = chargingStationService.updateChargingStation(1, updatedChargingStation);

    assertThat(result).isEqualTo(updatedChargingStation);
  }

  @Test
  void whenUpdateChargingStationWithFullData_thenReturnUpdatedChargingStation() {
    ChargingStation updatedChargingStation = new ChargingStation();
    updatedChargingStation.setId(1);
    updatedChargingStation.setName("Updated Station");
    updatedChargingStation.setLat(BigDecimal.valueOf(40.7128));
    updatedChargingStation.setLon(BigDecimal.valueOf(-74.0060));
    updatedChargingStation.setPhotoUrl("https://example.com/updated_photo.jpg");
    updatedChargingStation.setOperator(operator1);

    when(userRepository.findById(1)).thenReturn(Optional.of(operator1));
    when(chargingStationRepository.findById(1)).thenReturn(Optional.of(chargingStation1));
    when(chargingStationRepository.save(any(ChargingStation.class)))
            .thenReturn(updatedChargingStation);

    ChargingStation result =
            chargingStationService.updateChargingStation(1, updatedChargingStation);

    assertThat(result).isEqualTo(updatedChargingStation);
  }

  @Test
  void whenUpdateChargingStation_withInvalidData_thenReturnNull() {
    ChargingStation invalidChargingStation = new ChargingStation();
    invalidChargingStation.setId(1);
    invalidChargingStation.setName("");

    ChargingStation result =
            chargingStationService.updateChargingStation(1, invalidChargingStation);

    assertThat(result).isNull();
  }

  @Test
  void whenDeleteChargingStation_thenReturnTrue() {
    when(chargingStationRepository.findById(1)).thenReturn(Optional.of(chargingStation1));

    boolean result = chargingStationService.deleteChargingStation(1, 1);

    assertThat(result).isTrue();
    verify(chargingStationRepository).findById(1);
  }

  @Test
  void whenDeleteChargingStation_withInvalidId_thenReturnFalse() {
    when(chargingStationRepository.findById(999)).thenReturn(Optional.empty());

    boolean result = chargingStationService.deleteChargingStation(999, 1);

    assertThat(result).isFalse();
    verify(chargingStationRepository).findById(999);
  }

  @Test
  void whenDeleteChargingStation_withInvalidOperatorId_thenReturnFalse() {
    when(chargingStationRepository.findById(1)).thenReturn(Optional.of(chargingStation1));

    boolean result = chargingStationService.deleteChargingStation(1, 999);

    assertThat(result).isFalse();
    verify(chargingStationRepository).findById(1);
  }

  @Test
  void whenFilterChargingStations_returnMatchingStations(){
    ChargingSpot chargingSpot1 = new ChargingSpot();
    chargingSpot1.setId(1);
    chargingSpot1.setConnectorType(ConnectorType.CCS);

    ChargingSpot chargingSpot2 = new ChargingSpot();
    chargingSpot2.setId(2);
    chargingSpot2.setConnectorType(ConnectorType.CHADEMO);

    ChargingSpot chargingSpot3 = new ChargingSpot();
    chargingSpot3.setId(3);
    chargingSpot3.setConnectorType(ConnectorType.SAEJ1772);

    ChargingSpot chargingSpot4 = new ChargingSpot();
    chargingSpot4.setId(4);
    chargingSpot4.setConnectorType(ConnectorType.CCS);

    ChargingSpot chargingSpot5 = new ChargingSpot();
    chargingSpot5.setId(5);
    chargingSpot5.setConnectorType(ConnectorType.MENNEKES);

    ChargingSpot chargingSpot6 = new ChargingSpot();
    chargingSpot6.setId(6);
    chargingSpot6.setConnectorType(ConnectorType.CHADEMO);

    ChargingStation chargingStation4 = new ChargingStation();
    chargingStation4.setId(4);
    chargingStation4.setName("Charging Station 1");
    chargingStation4.setLat(BigDecimal.valueOf(40.7128));
    chargingStation4.setLon(BigDecimal.valueOf(-74.0060));
    chargingStation4.setPhotoUrl("https://example.com/photo.jpg");
    chargingStation4.setOperator(operator1);


    List<ChargingStation> chargingStations = List.of(chargingStation1, chargingStation2, chargingStation3, chargingStation4);
    List<ConnectorType> connectorTypes = List.of(ConnectorType.CCS, ConnectorType.CHADEMO);
    List<ChargingSpot> chargingSpots1 = List.of(chargingSpot1, chargingSpot2, chargingSpot3);
    List<ChargingSpot> chargingSpots2 = List.of(chargingSpot4, chargingSpot5);
    List<ChargingSpot> chargingSpots3 = List.of(chargingSpot6);


    when(chargingStationRepository.findAll()).thenReturn(chargingStations);
    when(chargingSpotRepository.findAllByStation_Id(1)).thenReturn(Optional.of(chargingSpots1));
    when(chargingSpotRepository.findAllByStation_Id(2)).thenReturn(Optional.of(chargingSpots2));
    when(chargingSpotRepository.findAllByStation_Id(3)).thenReturn(Optional.of(chargingSpots3));
    when(chargingSpotRepository.findAllByStation_Id(4)).thenReturn(Optional.empty());

    List<ChargingStation> result = chargingStationService.filterChargingStations(connectorTypes);

    assertThat(result).isEqualTo(List.of(chargingStation1));
  }
}