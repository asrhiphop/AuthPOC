package com.example.authpoc.user;

import com.example.authpoc.exception.ResourceNotFoundException;
import com.example.authpoc.security.config.SecurityConfig;
import com.example.authpoc.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("GET /api/user/me without a token should return 401 problem+json")
    void me_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @DisplayName("GET /api/user/me without 'profile:read' should return 403 problem+json")
    void me_WrongAuthority_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/user/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin:user:read"))
                                .jwt(b -> b.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("GET /api/user/me with 'profile:read' should return the caller's profile")
    void me_WithAuthority_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getUser(id)).thenReturn(new UserResponse(id, "john_doe", "john@example.com", Role.USER));

        mockMvc.perform(get("/api/user/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("profile:read"))
                                .jwt(b -> b.subject(id.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"));
    }

    @Test
    @DisplayName("GET /api/user/me for a user deleted after the token was issued should return 404 problem+json")
    void me_DeletedUser_ShouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getUser(id)).thenThrow(new ResourceNotFoundException("User", "id", id));

        mockMvc.perform(get("/api/user/me")
                        .with(jwt().authorities(new SimpleGrantedAuthority("profile:read"))
                                .jwt(b -> b.subject(id.toString()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
