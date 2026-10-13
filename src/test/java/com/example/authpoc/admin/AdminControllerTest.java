package com.example.authpoc.admin;

import com.example.authpoc.admin.dto.UpdateUserRoleRequest;
import com.example.authpoc.admin.dto.UpdateUserStatusRequest;
import com.example.authpoc.security.config.SecurityConfig;
import com.example.authpoc.user.Role;
import com.example.authpoc.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("GET /api/admin/users without token should return 401 Unauthorized")
    void getUsers_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @DisplayName("GET /api/admin/users with invalid bearer token should return problem+json 401")
    void getUsers_InvalidToken_ShouldReturn401() throws Exception {
        when(jwtDecoder.decode(any())).thenThrow(new org.springframework.security.oauth2.jwt.BadJwtException("bad token detail"));

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("bad token detail"))));
    }

    @Test
    @DisplayName("GET /api/admin/users with wrong permission should return 403 Forbidden")
    void getUsers_InsufficientPermission_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("profile:read"))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("GET /api/admin/users with 'admin:user:read' authority should return 200 OK")
    void getUsers_WithRequiredPermission_ShouldReturn200() throws Exception {
        Page<UserResponse> emptyPage = new PageImpl<>(List.of());
        when(adminService.getUsers(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin:user:read"))))
                .andExpect(status().isOk());

        verify(adminService).getUsers(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/admin/users/{id} with 'admin:user:read' authority should return 200 OK")
    void getUser_WithRequiredPermission_ShouldReturn200() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse mockUser = new UserResponse(userId, "john_doe", "john@example.com", Role.USER);

        when(adminService.getUser(userId)).thenReturn(mockUser);

        mockMvc.perform(get("/api/admin/users/{id}", userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("admin:user:read"))))
                .andExpect(status().isOk());

        verify(adminService).getUser(userId);
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/enabled with 'admin:user:write' authority should return 204 No Content")
    void updateUserStatus_WithRequiredPermission_ShouldReturn204() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UpdateUserStatusRequest requestBody = new UpdateUserStatusRequest(false);

        mockMvc.perform(put("/api/admin/users/{id}/enabled", targetUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("admin:user:write"))
                                .jwt(builder -> builder.subject(adminId.toString()))))
                .andExpect(status().isNoContent());

        verify(adminService).updateUserStatus(eq(adminId), eq(targetUserId), any(UpdateUserStatusRequest.class));
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id}/role with 'admin:user:write' authority should return 204 No Content")
    void updateUserRole_WithRequiredPermission_ShouldReturn204() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UpdateUserRoleRequest requestBody = new UpdateUserRoleRequest(Role.ADMIN);

        mockMvc.perform(put("/api/admin/users/{id}/role", targetUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("admin:user:write"))
                                .jwt(builder -> builder.subject(adminId.toString()))))
                .andExpect(status().isNoContent());

        verify(adminService).updateUserRole(eq(adminId), eq(targetUserId), any(UpdateUserRoleRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/admin/users/{id} with 'admin:user:delete' authority should return 204 No Content")
    void deleteUser_WithRequiredPermission_ShouldReturn204() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/users/{id}", targetUserId)
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("admin:user:delete"))
                                .jwt(builder -> builder.subject(adminId.toString()))))
                .andExpect(status().isNoContent());

        verify(adminService).deleteUser(adminId, targetUserId);
    }
}
