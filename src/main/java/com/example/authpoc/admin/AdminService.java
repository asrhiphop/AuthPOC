package com.example.authpoc.admin;

import com.example.authpoc.admin.dto.UpdateUserRoleRequest;
import com.example.authpoc.admin.dto.UpdateUserStatusRequest;
import com.example.authpoc.exception.SelfOperationException;
import com.example.authpoc.user.UserService;
import com.example.authpoc.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService {
    private final UserService userService;

    public AdminService(UserService userService) {
        this.userService = userService;
    }

    public Page<UserResponse> getUsers(Pageable pageable) {
        return userService.getUsers(pageable);
    }

    public UserResponse getUser(UUID id) {
        return userService.getUser(id);
    }

    public void updateUserStatus(UUID userId, UUID targetId, UpdateUserStatusRequest request) {
        if (userId.equals(targetId)) {
            throw new SelfOperationException("Administrators cannot disable their own accounts.");
        }
        userService.updateUserStatus(targetId, request.enabled());
    }

    public void updateUserRole(UUID userId, UUID targetId, UpdateUserRoleRequest request) {
        if (userId.equals(targetId)) {
            throw new SelfOperationException("Administrators cannot change their own role.");
        }

        userService.updateUserRole(targetId, request.role());
    }

    public void deleteUser(UUID userId, UUID targetId) {
        if (userId.equals(targetId)) {
            throw new SelfOperationException("Administrators cannot delete their own accounts.");
        }

        userService.deleteUser(targetId);
    }
}
