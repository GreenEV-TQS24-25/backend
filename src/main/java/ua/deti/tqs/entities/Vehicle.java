package ua.deti.tqs.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ua.deti.tqs.entities.types.ConnectorType;

@Getter
@Setter
@Entity
@ToString
@Table(name = "vehicle")
public class Vehicle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  @JoinColumn(name = "user_id")
  private User user;

  @Size(max = 50)
  @NotNull
  @Column(name = "brand", nullable = false, length = 50)
  private String brand;

  @Size(max = 50)
  @NotNull
  @Column(name = "model", nullable = false, length = 50)
  private String model;

  @Size(max = 20)
  @NotNull
  @Column(name = "license_plate", nullable = false, length = 20, unique = true)
  private String licensePlate;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "connector_type")
  private ConnectorType connectorType = ConnectorType.SAEJ1772;
}
