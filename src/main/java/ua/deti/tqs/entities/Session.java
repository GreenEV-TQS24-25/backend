package ua.deti.tqs.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "session")
public class Session {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @NotNull
  @Column(name = "uuid", nullable = false)
  private String uuid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private Vehicle vehicle;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "charging_spot_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private ChargingSpot chargingSpot;

  @NotNull
  @Column(name = "start_time", nullable = false)
  private Instant startTime;

  @Column(name = "duration", nullable = false)
  private int duration = 30;

  @Column(name = "total_cost", precision = 8, scale = 2)
  private BigDecimal totalCost;

  @PrePersist
  public void generateTokenAndTimestamp() {
    this.uuid = UUID.randomUUID().toString();
  }
}
