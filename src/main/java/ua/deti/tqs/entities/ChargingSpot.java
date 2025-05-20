package ua.deti.tqs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import ua.deti.tqs.entities.types.ConnectorType;
import ua.deti.tqs.entities.types.Sonic;
import ua.deti.tqs.entities.types.SpotState;

@Getter
@Setter
@Entity
@Table(name = "charging_spot")
public class ChargingSpot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "station_id")
  private ChargingStation station;

  @NotNull
  @Column(name = "power_kw", nullable = false, precision = 7, scale = 2)
  private BigDecimal powerKw;

  @NotNull
  @Column(name = "price_per_kwh", nullable = false, precision = 8, scale = 2)
  private BigDecimal pricePerKwh;

  @Enumerated(EnumType.STRING)
  @Column(name = "charging_velocity", columnDefinition = "sonic not null")
  private Sonic chargingVelocity = Sonic.NORMAL;

  @Enumerated(EnumType.STRING)
  @Column(name = "connector_type", columnDefinition = "connector_type not null")
  private ConnectorType connectorType = ConnectorType.SAEJ1772;

  @Enumerated(EnumType.STRING)
  @Column(name = "state", columnDefinition = "spot_state not null")
  private SpotState state = SpotState.FREE;
}
