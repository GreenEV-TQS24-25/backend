package ua.deti.tqs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "price")
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "price_per_kwh", nullable = false, precision = 8, scale = 2)
    private BigDecimal pricePerKwh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charging_station_id")
    private ChargingStation chargingStation;

/*
 TODO [Reverse Engineering] create field to map the 'connector_type' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "connector_type", columnDefinition = "connector_type not null")
    private Object connectorType;
*/
/*
 TODO [Reverse Engineering] create field to map the 'charging_velocity' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "charging_velocity", columnDefinition = "sonic not null")
    private Object chargingVelocity;
*/
}