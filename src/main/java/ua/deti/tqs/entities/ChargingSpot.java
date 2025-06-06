package ua.deti.tqs.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ua.deti.tqs.entities.types.ConnectorType;
import ua.deti.tqs.entities.types.Sonic;
import ua.deti.tqs.entities.types.SpotState;

@Getter
@Setter
@Entity
@Table(name = "charging_spot")
@ToString
public class ChargingSpot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "station_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private ChargingStation station;

  @NotNull
  @Column(name = "power_kw", nullable = false, precision = 7, scale = 2)
  private BigDecimal powerKw;

  @NotNull
  @Column(name = "price_per_kwh", nullable = false, precision = 8, scale = 2)
  private BigDecimal pricePerKwh;

  @Enumerated(EnumType.STRING)
  @Column(name = "charging_velocity", nullable = false)
  private Sonic chargingVelocity = Sonic.NORMAL;

  @Enumerated(EnumType.STRING)
  @Column(name = "connector_type", nullable = false)
  private ConnectorType connectorType = ConnectorType.SAEJ1772;

  @Enumerated(EnumType.STRING)
  @Column(name = "state", nullable = false)
  private SpotState state = SpotState.FREE;
}
