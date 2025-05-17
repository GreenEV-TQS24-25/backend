package ua.deti.tqs.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.deti.tqs.repositories.UserTableRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserTableRepository userTableRepository;

    @Override
    public UserDetails loadUserByUsername(String name) {
        return userTableRepository.findByName(name).orElseThrow(() -> new UsernameNotFoundException("User não encontrado: " + name));
    }
}
