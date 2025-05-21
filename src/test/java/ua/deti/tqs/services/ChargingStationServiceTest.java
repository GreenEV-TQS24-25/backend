package ua.deti.tqs.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.entities.UserTable;
import ua.deti.tqs.entities.types.Role;
import ua.deti.tqs.repositories.ChargingStationRepository;
import ua.deti.tqs.repositories.UserTableRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ChargingStationServiceTest {

    @Mock
    private ChargingStationRepository chargingStationRepository;

    @Mock
    private UserTableRepository userTableRepository;

    @InjectMocks
    private ChargingStationServiceImpl chargingStationService;

    private ChargingStation chargingStation1;
    private ChargingStation chargingStation2;

    private UserTable operator1;
    private UserTable operator2;

    @BeforeEach
    void setUp() {
        chargingStation1 = new ChargingStation();
        chargingStation1.setId(1);
        chargingStation1.setName("Charging Station 1");
        chargingStation1.setLat(BigDecimal.valueOf(40.7128));
        chargingStation1.setLon(BigDecimal.valueOf(-74.0060));
        chargingStation1.setPhotoUrl("https://example.com/photo.jpg");
        chargingStation1.setLastMaintenance(LocalDate.parse("2023-01-01"));

        chargingStation2 = new ChargingStation();
        chargingStation2.setId(2);
        chargingStation2.setName("Charging Station 2");
        chargingStation2.setLat(BigDecimal.valueOf(24.7128));
        chargingStation2.setLon(BigDecimal.valueOf(111.0060));
        chargingStation2.setPhotoUrl("https://example.com/photo.jpg");
        chargingStation2.setLastMaintenance(LocalDate.parse("2023-01-01"));

        operator1 = new UserTable();
        operator1.setId(1);
        operator1.setRole(Role.OPERATOR);
        chargingStation1.setOperator(operator1);

        operator2 = new UserTable();
        operator2.setId(2);
        operator2.setRole(Role.OPERATOR);
        chargingStation2.setOperator(operator2);

    }

    @Test
    void whenGetAllChargingStationsByOperatorId_thenReturnList() {
        List<ChargingStation> chargingStations = List.of(chargingStation1);
        when(chargingStationRepository.findAllByOperator_Id(1)).thenReturn(Optional.of(chargingStations));

        List<ChargingStation> result = chargingStationService.getAllChargingStationsByOperatorId(1);

        assertThat(result).isEqualTo(chargingStations);
        verify(chargingStationRepository).findAllByOperator_Id(1);
    }

    @Test
    void whenGetAllChargingStations_withNullOperatorId_thenReturnEmptyList() {
        when(chargingStationRepository.findAllByOperator_Id(1)).thenReturn(Optional.empty());

        List<ChargingStation> result = chargingStationService.getAllChargingStationsByOperatorId(1);

        assertThat(result).isEmpty();
        verify(chargingStationRepository).findAllByOperator_Id(1);
    }

    @Test
    void whenGetAllChargingStationsByOperatorId_withInvalidOperatorId_thenReturnEmptyList() {
        when(chargingStationRepository.findAllByOperator_Id(999)).thenReturn(Optional.empty());

        List<ChargingStation> result = chargingStationService.getAllChargingStationsByOperatorId(999);

        assertThat(result).isEmpty();
        verify(chargingStationRepository).findAllByOperator_Id(999);
    }

    @Test
    void whenGetAllChargingStations_thenReturnList() {
        List<ChargingStation> chargingStations = List.of(chargingStation1);
        when(chargingStationRepository.findAll()).thenReturn(chargingStations);

        List<ChargingStation> result = chargingStationService.getAllChargingStations();

        assertThat(result).isEqualTo(chargingStations);
        verify(chargingStationRepository).findAll();
    }

    @Test
    void whenGetAllChargingStations_withEmptyList_thenReturnEmptyList() {
        when(chargingStationRepository.findAll()).thenReturn(List.of());

        List<ChargingStation> result = chargingStationService.getAllChargingStations();

        assertThat(result).isEmpty();
        verify(chargingStationRepository).findAll();
    }

    @Test
    void whenCreateChargingStation_thenReturnChargingStation() {
        when(userTableRepository.findById(1)).thenReturn(Optional.of(operator1));
        when(chargingStationRepository.save(any(ChargingStation.class))).thenReturn(chargingStation1);

        ChargingStation result = chargingStationService.createChargingStation(chargingStation1);

        assertThat(result).isEqualTo(chargingStation1);
    }

    @Test
    void whenCreateChargingStation_withTheBasicInfo_thenReturnChargingStation() {
        when(userTableRepository.findById(1)).thenReturn(Optional.of(operator1));
        chargingStation1.setPhotoUrl(null);
        chargingStation1.setLastMaintenance(null);

        when(chargingStationRepository.save(any(ChargingStation.class))).thenReturn(chargingStation1);

        ChargingStation result = chargingStationService.createChargingStation(chargingStation1);

        assertThat(result).isEqualTo(chargingStation1);
    }

    @Test
    void whenCreateChargingStation_withInvalidData_thenReturnNull() {
        ChargingStation invalidChargingStation = new ChargingStation();
        invalidChargingStation.setName(null);
        invalidChargingStation.setLat(null);
        invalidChargingStation.setLon(null);


        ChargingStation result = chargingStationService.createChargingStation(invalidChargingStation);

        assertThat(result).isNull();
    }

    @Test
    void whenCreateChargingStation_withPartialInvalidData_thenReturnNull() {
        ChargingStation invalidChargingStation = new ChargingStation();
        invalidChargingStation.setName("");
        invalidChargingStation.setLat(BigDecimal.valueOf(100.0));
        invalidChargingStation.setLon(null);

        ChargingStation result = chargingStationService.createChargingStation(invalidChargingStation);

        assertThat(result).isNull();
    }

    @Test
    void whenCreateChargingStation_withNullOperator_thenReturnNull() {
        ChargingStation invalidChargingStation = new ChargingStation();
        invalidChargingStation.setName("Invalid Station");
        invalidChargingStation.setLat(BigDecimal.valueOf(40.7128));
        invalidChargingStation.setLon(BigDecimal.valueOf(-74.0060));
        invalidChargingStation.setOperator(null);


        ChargingStation result = chargingStationService.createChargingStation(invalidChargingStation);

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

        when(userTableRepository.findById(1)).thenReturn(Optional.of(operator1));

        ChargingStation result = chargingStationService.createChargingStation(invalidChargingStation);
        assertThat(result).isNull();
    }

    @Test
    void whenCreateChargingStation_withOperatorNotFound_thenReturnNull() {
        ChargingStation invalidChargingStation = new ChargingStation();
        invalidChargingStation.setName("Invalid Station");
        invalidChargingStation.setLat(BigDecimal.valueOf(40.7128));
        invalidChargingStation.setLon(BigDecimal.valueOf(-74.0060));

        invalidChargingStation.setOperator(operator1);

        when(userTableRepository.findById(1)).thenReturn(Optional.empty());

        ChargingStation result = chargingStationService.createChargingStation(invalidChargingStation);
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
        updatedChargingStation.setLastMaintenance(LocalDate.parse("2023-01-01"));
        updatedChargingStation.setOperator(operator1);

        when(chargingStationRepository.findById(1)).thenReturn(Optional.of(chargingStation1));
        when(chargingStationRepository.save(any(ChargingStation.class))).thenReturn(updatedChargingStation);

        ChargingStation result = chargingStationService.updateChargingStation(1, updatedChargingStation);
        assertThat(result).isEqualTo(updatedChargingStation);
    }

    @Test
    void whenUpdateChargingStation_withInvalidOperatorId_thenReturnNull() {
        when(chargingStationRepository.findById(1)).thenReturn(Optional.empty());

        ChargingStation result = chargingStationService.updateChargingStation(999, chargingStation1);

        assertThat(result).isNull();
    }

    @Test
    void whenUpdateChargingStation_withInvalidOperator_thenReturnNull() {
        when(chargingStationRepository.findById(1)).thenReturn(Optional.of(chargingStation1));

        ChargingStation result = chargingStationService.updateChargingStation(999, chargingStation1);

        assertThat(result).isNull();
        verify(chargingStationRepository).findById(1);
    }

    @Test
    void whenUpdateChargingStation_withNullData_thenReturnNull() {
        ChargingStation invalidChargingStation = new ChargingStation();
        invalidChargingStation.setId(1);
        invalidChargingStation.setName(null);
        invalidChargingStation.setLat(null);
        invalidChargingStation.setLon(null);
        invalidChargingStation.setPhotoUrl(null);
        invalidChargingStation.setLastMaintenance(null);
        invalidChargingStation.setOperator(operator1);


        when(chargingStationRepository.findById(1)).thenReturn(Optional.of(chargingStation1));

        ChargingStation result = chargingStationService.updateChargingStation(1, invalidChargingStation);

        assertThat(result).isNull();
    }

    @Test
    void whenUpdateChargingStation_withInvalidData_thenReturnNull() {
        ChargingStation invalidChargingStation = new ChargingStation();
        invalidChargingStation.setId(1);
        invalidChargingStation.setName("");

        when(chargingStationRepository.findById(1)).thenReturn(Optional.of(chargingStation1));
        ChargingStation result = chargingStationService.updateChargingStation(1, invalidChargingStation);

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
    void whenGetFilteredChargingStations_withMultipleMatches_thenReturnList() {
        List<ChargingStation> chargingStations = List.of(chargingStation1, chargingStation2);
        List<Integer> operatorIds = List.of(1, 2);
        when(chargingStationRepository.findAll()).thenReturn(chargingStations);
        List<ChargingStation> result = chargingStationService.getFilteredChargingStations(operatorIds);
        assertThat(result).isEqualTo(chargingStations);
    }

    @Test 
    void whenGetFilteredChargingStations_withMismatches_thenReturnList() {
        List<ChargingStation> chargingStations = List.of(chargingStation1, chargingStation2);
        List<Integer> operatorIds = List.of(1);
        when(chargingStationRepository.findAll()).thenReturn(chargingStations);
        List<ChargingStation> result = chargingStationService.getFilteredChargingStations(operatorIds);
        assertThat(result).isEqualTo(List.of(chargingStation1));
    }

    @Test 
    void whenGetFilteredChargingStations_withNoMatches_thenReturnEmptyList() {
        List<ChargingStation> chargingStations = List.of(chargingStation1, chargingStation2);
        List<Integer> operatorIds = List.of(3, 4, 5);
        when(chargingStationRepository.findAll()).thenReturn(chargingStations);
        List<ChargingStation> result = chargingStationService.getFilteredChargingStations(operatorIds);
        assertThat(result).isEqualTo(List.of());
    }

}
