package ua.deti.tqs.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ua.deti.tqs.entities.ChargingStation;
import ua.deti.tqs.entities.User;
import ua.deti.tqs.entities.types.Role;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ChargingStationRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    private User operator1;

    @BeforeEach
    void setUp() {
        // given
        entityManager.clear();

        operator1 = new User();
        operator1.setId(1);
        operator1.setName("Operator 1");
        operator1.setPassword("password");
        operator1.setEmail("email");
        operator1.setRole(Role.OPERATOR);
        entityManager.persist(operator1);

        ChargingStation chargingStation1 = new ChargingStation();
        chargingStation1.setId(1);
        chargingStation1.setName("Charging Station 1");
        chargingStation1.setLat(BigDecimal.valueOf(40.7128));
        chargingStation1.setLon(BigDecimal.valueOf(-74.0060));
        chargingStation1.setOperator(operator1);
        entityManager.persist(chargingStation1);

        entityManager.flush();
    }

    @Test
    void whenFindAllByOperator_Id_thenReturnChargingStations() {
        // when
        Optional<List<ChargingStation>> found = chargingStationRepository.findAllByOperator_Id(operator1.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get()).isNotEmpty();
    }


    @Test
    void whenFindAllByOperator_Id_thenReturnEmptyList() {
        // when
        Optional<List<ChargingStation>> found = chargingStationRepository.findAllByOperator_Id(2);

        // then
        assertThat(found).isPresent();
        assertThat(found.get()).isEmpty();
    }

}