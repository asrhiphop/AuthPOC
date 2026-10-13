package com.example.authpoc.user;

import com.example.authpoc.exception.AccountLockedException;
import com.example.authpoc.exception.InvalidPasswordException;
import com.example.authpoc.exception.ResourceAlreadyExistsException;
import com.example.authpoc.exception.ResourceNotFoundException;
import com.example.authpoc.exception.UnknownUsernameException;
import com.example.authpoc.security.config.LockoutProperties;
import com.example.authpoc.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LockoutProperties lockoutProperties;
    private final Clock clock;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            LockoutProperties lockoutProperties,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.lockoutProperties = lockoutProperties;
        this.clock = clock;
    }

    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromEntity);
    }

    public UserResponse getUser(UUID id) {
        return userRepository.findById(id)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional(readOnly = true)
    public UserResponse verifyCredentials(String username, String rawPassword) {
        Instant now = clock.instant();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnknownUsernameException(username));

        if (user.isLocked(now)) {
            throw new AccountLockedException(username);
        }

        if (!user.matchesPassword(rawPassword, passwordEncoder)) {
            throw new InvalidPasswordException(username);
        }

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public void recordFailedAttempt(String username) {
        Instant now = clock.instant();
        userRepository.findByUsername(username).ifPresent(user -> {
            user.incrementFailedAttempts(
                    lockoutProperties.maxAttempts(),
                    lockoutProperties.duration(),
                    now
            );
            userRepository.save(user);
        });
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0) {
                user.resetFailedAttempts();
                userRepository.save(user);
            }
        });
    }

    @Transactional
    public void recordLogin(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.recordLogin(clock.instant());
    }

    @Transactional
    public UserResponse createUser(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("User", "username", username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("User", "email", email);
        }

        String passwordHash = passwordEncoder.encode(rawPassword);

        User user = new User(
                username,
                email,
                passwordHash
        );

        User savedUser = userRepository.save(user);

        return UserResponse.fromEntity(savedUser);
    }

    @Transactional
    public void updateUserStatus(UUID userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setEnabled(enabled);
    }

    @Transactional
    public void updateUserRole(UUID userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setRole(role);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        getUser(userId); // Validates existence & throws ResourceNotFoundException

        userRepository.deleteById(userId);
    }
}
