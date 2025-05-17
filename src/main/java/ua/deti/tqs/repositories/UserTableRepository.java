package ua.deti.tqs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.deti.tqs.entities.UserTable;

import java.util.Optional;

public interface UserTableRepository extends JpaRepository<UserTable, Integer> {
    Optional<UserTable> findByName(String name);
}
