package com.example.authpoc.security;

import com.example.authpoc.security.config.AdminProperties;
import com.example.authpoc.user.Role;
import com.example.authpoc.user.User;
import com.example.authpoc.user.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties properties;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AdminProperties properties
    ) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @PostConstruct
    public void createAdmin() {
        if (userRepository.findByUsername(properties.username()).isPresent()) {
            return;
        }

        User admin = new User(
                properties.username(),
                properties.email(),
                passwordEncoder.encode(properties.password()),
                Role.ADMIN
        );

        userRepository.save(admin);
    }
}