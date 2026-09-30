package com.example.authpoc.auth;

import com.example.authpoc.auth.dto.LoginRequest;
import com.example.authpoc.auth.dto.RegisterRequest;
import com.example.authpoc.auth.model.RegisterResult;
import com.example.authpoc.exception.EmailAlreadyExistsException;
import com.example.authpoc.exception.InvalidCredentialsException;
import com.example.authpoc.exception.UsernameAlreadyExistsException;
import com.example.authpoc.security.JwtTokenService;
import com.example.authpoc.user.User;
import com.example.authpoc.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
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

    public String login(LoginRequest request) throws InvalidCredentialsException {
        Optional<User> user = userRepository.findByUsername(request.username());

        if (user.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        User authenticatedUser = user.get();

        boolean match = authenticatedUser.matchesPassword(
                request.password(),
                passwordEncoder
        );

        if (!match) {
            throw new InvalidCredentialsException();
        }

        return jwtTokenService.generateToken(authenticatedUser);
    }
}