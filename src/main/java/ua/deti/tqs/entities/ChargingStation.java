package ua.deti.tqs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ua.deti.tqs.entities.types.ConnectorType;

import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "charging_station")
public class ChargingStation {
    @Id
    @ColumnDefault("nextval('charging_station_id_seq')")
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
    private User operator;

    @Size(max = 255)
    @Column(name = "photo_url")
    private String photoUrl;

    @JsonIgnore
    @OneToMany(mappedBy = "station")
    private List<ChargingSpot> chargingSpots;

    public List<ConnectorType> getConnectorTypes(){
        List<ConnectorType> connectorTypes = new ArrayList<>();
        for (ChargingSpot chargingSpot : chargingSpots) {
            connectorTypes.add(chargingSpot.getConnectorType());
        }
        return connectorTypes;
    }

}