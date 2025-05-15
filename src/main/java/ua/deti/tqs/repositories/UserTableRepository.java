package ua.deti.tqs.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.deti.tqs.entities.UserTable;

public interface UserTableRepository extends JpaRepository<UserTable, Integer> {
}
