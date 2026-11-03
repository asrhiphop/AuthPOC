package com.example.authpoc.security;

import com.example.authpoc.security.config.AdminProperties;
import com.example.authpoc.user.Role;
import com.example.authpoc.user.User;
import com.example.authpoc.user.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties properties;
    private final Validator validator;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AdminProperties properties,
            Validator validator
    ) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.validator = validator;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        validateConfiguration();

        User existingUser = userRepository.findByUsername(properties.username()).orElse(null);
        if (existingUser != null) {
            if (existingUser.getRole() != Role.ADMIN) {
                throw new IllegalStateException(
                        "Admin bootstrap cannot use username '" + properties.username()
                                + "' because it belongs to a non-admin user."
                );
            }
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

    private void validateConfiguration() {
        Set<ConstraintViolation<AdminProperties>> violations = validator.validate(properties);
        if (violations.isEmpty()) {
            return;
        }

        String invalidProperties = violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));

        throw new IllegalStateException(
                "Admin bootstrap configuration is invalid for: " + invalidProperties
        );
    }
}
