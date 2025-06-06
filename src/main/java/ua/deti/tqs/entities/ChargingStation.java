package ua.deti.tqs.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "charging_station")
public class ChargingStation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, unique = true)
  private Integer id;

  @Size(max = 100)
  @NotNull
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @NotNull
  @Column(name = "lat", nullable = false, precision = 9, scale = 6)
  private BigDecimal lat;

  @NotNull
  @Column(name = "lon", nullable = false, precision = 9, scale = 6)
  private BigDecimal lon;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "operator_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private User operator;

  @Size(max = 255)
  @Column(name = "photo_url")
  private String photoUrl;
}
