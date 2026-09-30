package com.example.authpoc.auth;

import com.example.authpoc.auth.dto.LoginRequest;
import com.example.authpoc.auth.dto.RegisterRequest;
import com.example.authpoc.auth.model.RegisterResult;
import com.example.authpoc.exception.AuthenticationException;
import com.example.authpoc.exception.EmailAlreadyExistsException;
import com.example.authpoc.exception.UsernameAlreadyExistsException;
import com.example.authpoc.user.User;
import com.example.authpoc.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResult register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException();
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = new User(
                request.username(),
                request.email(),
                passwordHash
        );

        User savedUser = userRepository.save(user);

        return new RegisterResult(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    public void login(LoginRequest request) throws AuthenticationException {
        Optional<User> user = userRepository.findByUsername(request.username());

        if (user.isEmpty() ||
                !user.get().matchesPassword(request.password(), passwordEncoder)) {
            throw new AuthenticationException();
        }
    }
}