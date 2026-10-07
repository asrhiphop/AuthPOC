package com.example.authpoc.auth;

import com.example.authpoc.auth.dto.LoginRequest;
import com.example.authpoc.auth.dto.RegisterRequest;
import com.example.authpoc.auth.model.RegisterResult;
import com.example.authpoc.security.JwtTokenService;
import com.example.authpoc.user.UserService;
import com.example.authpoc.user.dto.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserService userService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    public RegisterResult register(RegisterRequest request) {
        UserResponse savedUser = userService.createUser(
                request.username(),
                request.email(),
                request.password()
        );

        return RegisterResult.from(savedUser);
    }

    public String login(LoginRequest request) {
        UserResponse authenticatedUser = userService.verifyCredentials(
                request.username(),
                request.password()
        );

        userService.recordLogin(authenticatedUser.id());

        return jwtTokenService.generateToken(authenticatedUser);
    }
}