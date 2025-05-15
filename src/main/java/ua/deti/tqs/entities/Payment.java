package ua.deti.tqs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import ua.deti.tqs.entities.types.PaymentState;

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
    @Column(name = "price", nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

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

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDING'")
    @Column(name = "state", columnDefinition = "payment_state not null")
    private PaymentState state;

}