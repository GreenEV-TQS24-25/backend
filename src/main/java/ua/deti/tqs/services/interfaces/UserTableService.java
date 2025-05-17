package ua.deti.tqs.services.interfaces;

import ua.deti.tqs.entities.UserTable;

public interface UserTableService   {
    UserTable getUserById(int id);

    UserTable createUser(UserTable user);

    UserTable updateUser(int id, UserTable user);

    boolean deleteUser(int id);
}
