package ua.deti.tqs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "charging_station")
public class ChargingStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
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
    private UserTable operator;

    @Column(name = "last_maintenance")
    private LocalDate lastMaintenance;

    @Size(max = 255)
    @Column(name = "photo_url")
    private String photoUrl;

}