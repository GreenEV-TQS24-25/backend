package ua.deti.tqs.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import ua.deti.tqs.entities.types.Role;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private final Integer id;
    private final String name;
    private final String email;
    private Role role;
    private final String token;
    private final Long expires;
}