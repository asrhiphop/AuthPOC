package com.example.authpoc.auth;

import com.example.authpoc.auth.dto.LoginRequest;
import com.example.authpoc.auth.dto.RegisterRequest;
import com.example.authpoc.auth.dto.RegisterResponse;
import com.example.authpoc.auth.token.RefreshTokenService;
import com.example.authpoc.auth.token.dto.TokenResponse;
import com.example.authpoc.exception.InvalidCredentialsException;
import com.example.authpoc.security.JwtTokenService;
import com.example.authpoc.user.UserService;
import com.example.authpoc.user.dto.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserService userService,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    public RegisterResponse register(RegisterRequest request) {
        UserResponse savedUser = userService.createUser(
                request.username(),
                request.email(),
                request.password()
        );

        return new RegisterResponse(
                savedUser.id(),
                savedUser.username(),
                savedUser.email()
        );
    }

    public TokenResponse login(LoginRequest request) {
        UserResponse authenticatedUser;

        try {
            authenticatedUser = userService.verifyCredentials(
                    request.username(),
                    request.password()
            );
        } catch (InvalidCredentialsException ex) {
            userService.recordFailedAttempt(request.username());
            throw ex;
        }

        userService.resetFailedAttempts(request.username());
        userService.recordLogin(authenticatedUser.id());

        String accessToken = jwtTokenService.generateToken(authenticatedUser);
        String refreshToken = refreshTokenService.createRefreshToken(authenticatedUser.id());

        return new TokenResponse(accessToken, refreshToken);
    }
}