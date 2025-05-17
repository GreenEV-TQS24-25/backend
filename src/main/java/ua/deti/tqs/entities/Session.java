package ua.deti.tqs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "session")
public class Session {
    @Id
    @ColumnDefault("nextval('session_id_seq')")
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "uuid", nullable = false)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserTable user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charging_spot_id")
    private ChargingSpot chargingSpot;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "total_cost", precision = 8, scale = 2)
    private BigDecimal totalCost;


    @PrePersist
    public void generateTokenAndTimestamp() {
        this.uuid = UUID.randomUUID().toString();
    }

}