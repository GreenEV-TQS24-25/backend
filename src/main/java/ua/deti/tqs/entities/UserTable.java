package ua.deti.tqs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import ua.deti.tqs.entities.types.Role;

@Getter
@Setter
@Entity
@Table(name = "user_table")
public class UserTable {
    @Id
    @ColumnDefault("nextval('user_table_id_seq')")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'USER'")
    @Column(name = "role", columnDefinition = "role not null")
    private Role role;

}