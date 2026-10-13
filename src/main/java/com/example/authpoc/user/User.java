package com.example.authpoc.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant lastLoginAt;

    @Setter
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "lockout_until")
    private Instant lockoutUntil;

    public User(
            String username,
            String email,
            String passwordHash,
            Role role
    ) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public User(String username, String email, String passwordHash) {
        this(username, email, passwordHash, Role.USER);
    }

    public boolean matchesPassword(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(password, this.passwordHash);
    }

    public void recordLogin(Instant now) {
        this.lastLoginAt = Objects.requireNonNull(now, "now must not be null");
    }

    public boolean isLocked(Instant now) {
        Objects.requireNonNull(now, "now must not be null");

        if (lockoutUntil == null) {
            return false;
        }
        return now.isBefore(lockoutUntil);
    }

    public void incrementFailedAttempts(
            int maxAttempts,
            Duration lockoutDuration,
            Instant now
    ) {
        Objects.requireNonNull(lockoutDuration, "lockoutDuration must not be null");
        Objects.requireNonNull(now, "now must not be null");

        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.lockoutUntil = now.plus(lockoutDuration);
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockoutUntil = null;
    }
}
