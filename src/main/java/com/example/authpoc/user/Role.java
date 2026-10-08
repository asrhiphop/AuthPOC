package com.example.authpoc.user;

import lombok.Getter;

import java.util.Set;

@Getter
public enum Role {
    USER(Set.of(
            Permission.PROFILE_READ
    )),
    SUPERVISOR(Set.of(
            Permission.PROFILE_READ,
            Permission.ADMIN_USER_READ
    )),
    ADMIN(Set.of(
            Permission.PROFILE_READ,
            Permission.ADMIN_USER_READ,
            Permission.ADMIN_USER_WRITE,
            Permission.ADMIN_USER_DELETE
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

}