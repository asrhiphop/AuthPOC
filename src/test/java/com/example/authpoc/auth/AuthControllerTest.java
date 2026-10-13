package com.example.authpoc.auth;

import com.example.authpoc.auth.dto.LoginRequest;
import com.example.authpoc.auth.dto.RegisterRequest;
import com.example.authpoc.auth.dto.RegisterResponse;
import com.example.authpoc.auth.token.RefreshTokenService;
import com.example.authpoc.auth.token.dto.RefreshTokenRequest;
import com.example.authpoc.auth.token.dto.TokenResponse;
import com.example.authpoc.exception.InvalidPasswordException;
import com.example.authpoc.exception.InvalidTokenException;
import com.example.authpoc.exception.ResourceAlreadyExistsException;
import com.example.authpoc.security.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("POST /api/auth/register with valid data should return 201 without needing authentication")
    void register_Valid_ShouldReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "SecureP@ss123!");
        UUID id = UUID.randomUUID();
        when(authService.register(any())).thenReturn(new RegisterResponse(id, "john_doe", "john@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("john_doe"));
    }

    @Test
    @DisplayName("POST /api/auth/register with invalid data should return 400 with field errors")
    void register_Invalid_ShouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest("ab", "not-an-email", "");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.username").isArray())
                .andExpect(jsonPath("$.errors.email").isArray());
    }

    @Test
    @DisplayName("POST /api/auth/register with an existing user should return 409 problem+json")
    void register_Duplicate_ShouldReturn409() throws Exception {
        RegisterRequest request = new RegisterRequest("john_doe", "john@example.com", "SecureP@ss123!");
        when(authService.register(any()))
                .thenThrow(new ResourceAlreadyExistsException("User", "username", "john_doe"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("RESOURCE_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("POST /api/auth/login with valid credentials should return 200 and tokens")
    void login_Valid_ShouldReturn200() throws Exception {
        when(authService.login(any())).thenReturn(new TokenResponse("access", "refresh"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("john_doe", "pw"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    @DisplayName("POST /api/auth/login with a blank body should return 400")
    void login_Blank_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/auth/login with bad credentials should return a generic 401 that does not echo the username")
    void login_BadCredentials_ShouldReturnGeneric401() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidPasswordException("john_doe"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("john_doe", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"))
                .andExpect(jsonPath("$.detail").value("Invalid username or password."))
                .andExpect(content().string(not(containsString("john_doe"))));
    }

    @Test
    @DisplayName("POST /api/auth/refresh with a rejected token should return 401 and hide the internal reason")
    void refresh_InvalidToken_ShouldReturn401() throws Exception {
        when(refreshTokenService.refresh("bad"))
                .thenThrow(new InvalidTokenException("Security Alert: Token reuse detected. Session terminated."));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("bad"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"))
                .andExpect(content().string(not(containsString("reuse"))));
    }

    @Test
    @DisplayName("POST /api/auth/refresh with a valid token should return 200 and a new token pair")
    void refresh_Valid_ShouldReturn200() throws Exception {
        when(refreshTokenService.refresh("good")).thenReturn(new TokenResponse("new-access", "new-refresh"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("good"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh with a blank token should return 400")
    void refresh_Blank_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/auth/logout should revoke the token and return 204")
    void logout_ShouldReturn204() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("tok"))))
                .andExpect(status().isNoContent());

        verify(refreshTokenService).revokeToken("tok");
    }

    @Test
    @DisplayName("An unexpected service failure should return a generic 500 without leaking details")
    void unexpectedFailure_ShouldReturnGeneric500() throws Exception {
        doThrow(new IllegalStateException("jdbc:postgresql://db/secret"))
                .when(refreshTokenService).revokeToken(any());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("tok"))))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(not(containsString("jdbc"))));
    }
}
