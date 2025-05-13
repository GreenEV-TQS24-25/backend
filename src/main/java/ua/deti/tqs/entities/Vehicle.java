package ua.deti.tqs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import ua.deti.tqs.entities.types.ConnectorType;

@Getter
@Setter
@Entity
@Table(name = "vehicle")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicle_id_seq")
    @SequenceGenerator(name = "vehicle_id_seq", sequenceName = "vehicle_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserTable user;

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
    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "connector_type", columnDefinition = "connector_type not null")
    private ConnectorType connectorType;

}