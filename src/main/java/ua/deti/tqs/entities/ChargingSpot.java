package ua.deti.tqs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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
    @Column(name = "energy_kwh", nullable = false, precision = 7, scale = 2)
    private BigDecimal energyKwh;

/*
 TODO [Reverse Engineering] create field to map the 'charging_velocity' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "charging_velocity", columnDefinition = "sonic not null")
    private Object chargingVelocity;
*/
/*
 TODO [Reverse Engineering] create field to map the 'connector_type' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "connector_type", columnDefinition = "connector_type not null")
    private Object connectorType;
*/
/*
 TODO [Reverse Engineering] create field to map the 'state' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @ColumnDefault("'FREE'")
    @Column(name = "state", columnDefinition = "spot_state not null")
    private Object state;
*/
}