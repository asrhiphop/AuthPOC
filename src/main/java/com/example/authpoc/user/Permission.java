package com.example.authpoc.user;

import lombok.Getter;

@Getter
public enum Permission {
    // User Profile Permissions
    PROFILE_READ("profile:read"),

    // Administrative Permissions
    ADMIN_USER_READ("admin:user:read"),
    ADMIN_USER_WRITE("admin:user:write"),
    ADMIN_USER_DELETE("admin:user:delete");

    private final String value;

    Permission(String value) {
        this.value = value;
    }

}