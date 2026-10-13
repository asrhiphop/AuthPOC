package com.example.authpoc.admin;

import com.example.authpoc.admin.dto.UpdateUserRoleRequest;
import com.example.authpoc.admin.dto.UpdateUserStatusRequest;
import com.example.authpoc.exception.SelfOperationException;
import com.example.authpoc.user.Role;
import com.example.authpoc.user.UserService;
import com.example.authpoc.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("getUsers should delegate to UserService")
    void getUsers_ShouldDelegateToUserService() {
        Pageable pageable = Pageable.unpaged();
        Page<UserResponse> expectedPage = new PageImpl<>(List.of());
        when(userService.getUsers(pageable)).thenReturn(expectedPage);

        Page<UserResponse> result = adminService.getUsers(pageable);

        assertThat(result).isSameAs(expectedPage);
        verify(userService).getUsers(pageable);
    }

    @Test
    @DisplayName("getUser should delegate to UserService")
    void getUser_ShouldDelegateToUserService() {
        UUID userId = UUID.randomUUID();
        UserResponse expectedUser = new UserResponse(userId, "john_doe", "john@example.com", Role.USER);
        when(userService.getUser(userId)).thenReturn(expectedUser);

        UserResponse result = adminService.getUser(userId);

        assertThat(result).isSameAs(expectedUser);
        verify(userService).getUser(userId);
    }

    @Test
    @DisplayName("updateUserStatus on target user should delegate to UserService")
    void updateUserStatus_OnOtherUser_ShouldDelegateToUserService() {
        UUID adminId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(false);

        adminService.updateUserStatus(adminId, targetId, request);

        verify(userService).updateUserStatus(targetId, false);
    }

    @Test
    @DisplayName("updateUserStatus on self should throw SelfOperationException and not update status")
    void updateUserStatus_OnSelf_ShouldThrowException() {
        UUID adminId = UUID.randomUUID();
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(false);

        assertThatThrownBy(() -> adminService.updateUserStatus(adminId, adminId, request))
                .isInstanceOf(SelfOperationException.class)
                .hasMessage("Administrators cannot disable their own accounts.");

        verify(userService, never()).updateUserStatus(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    @DisplayName("updateUserRole on target user should delegate to UserService")
    void updateUserRole_OnOtherUser_ShouldDelegateToUserService() {
        UUID adminId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.ADMIN);

        adminService.updateUserRole(adminId, targetId, request);

        verify(userService).updateUserRole(targetId, Role.ADMIN);
    }

    @Test
    @DisplayName("updateUserRole on self should throw SelfOperationException and not update role")
    void updateUserRole_OnSelf_ShouldThrowException() {
        UUID adminId = UUID.randomUUID();
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.USER);

        assertThatThrownBy(() -> adminService.updateUserRole(adminId, adminId, request))
                .isInstanceOf(SelfOperationException.class)
                .hasMessage("Administrators cannot change their own role.");

        verify(userService, never()).updateUserRole(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("deleteUser on target user should delegate to UserService")
    void deleteUser_OnOtherUser_ShouldDelegateToUserService() {
        UUID adminId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        adminService.deleteUser(adminId, targetId);

        verify(userService).deleteUser(targetId);
    }

    @Test
    @DisplayName("deleteUser on self should throw SelfOperationException and not delete")
    void deleteUser_OnSelf_ShouldThrowException() {
        UUID adminId = UUID.randomUUID();

        assertThatThrownBy(() -> adminService.deleteUser(adminId, adminId))
                .isInstanceOf(SelfOperationException.class)
                .hasMessage("Administrators cannot delete their own accounts.");

        verify(userService, never()).deleteUser(org.mockito.ArgumentMatchers.any());
    }
}