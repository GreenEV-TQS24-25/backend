package ua.deti.tqs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @ColumnDefault("nextval('payment_id_seq')")
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @NotNull
    @Column(name = "value", nullable = false, precision = 8, scale = 2)
    private BigDecimal value;

    @Size(max = 30)
    @NotNull
    @Column(name = "method", nullable = false, length = 30)
    private String method;

    @Size(max = 100)
    @Column(name = "transaction_id", length = 100)
    private String transactionId;
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "date_hour")
    private Instant dateHour;

/*
 TODO [Reverse Engineering] create field to map the 'state' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @ColumnDefault("'PENDING'")
    @Column(name = "state", columnDefinition = "payment_state not null")
    private Object state;
*/
}