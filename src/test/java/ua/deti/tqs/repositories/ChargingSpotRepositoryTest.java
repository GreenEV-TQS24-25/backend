package ua.deti.tqs.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ua.deti.tqs.entities.ChargingSpot;
import ua.deti.tqs.entities.ChargingStation;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
@DataJpaTest
@ActiveProfiles("test")
class ChargingSpotRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChargingSpotRepository chargingSpotRepository;

    private ChargingStation chargingStation1;

    @BeforeEach
    void setUp() {
        // given
        entityManager.clear();

        chargingStation1 = new ChargingStation();
        chargingStation1.setId(1);
        chargingStation1.setName("Charging Station 1");
        chargingStation1.setLat(BigDecimal.valueOf(40.7128));
        chargingStation1.setLon(BigDecimal.valueOf(-74.0060));

        ChargingSpot chargingSpot1 = new ChargingSpot();
        chargingSpot1.setId(1);
        chargingSpot1.setStation(chargingStation1);
        chargingSpot1.setPowerKw(BigDecimal.TEN);
        chargingSpot1.setPricePerKwh(BigDecimal.valueOf(1.5));



        entityManager.persist(chargingStation1);
        entityManager.persist(chargingSpot1);
        entityManager.flush();
    }

    @Test
    void whenFindAllByStation_Id_thenReturnChargingSpots() {
        // when
        var found = chargingSpotRepository.findAllByStation_Id(chargingStation1.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get()).hasSize(1);
        assertThat(found.get().getFirst().getId()).isEqualTo(1);
    }

    @Test
    void whenFindAllByStation_Id_withInvalidId_thenReturnEmpty() {
        // when
        var found = chargingSpotRepository.findAllByStation_Id(999);

        // then
        assertThat(found).isPresent();
        assertThat(found.get()).isEmpty();
    }


}
