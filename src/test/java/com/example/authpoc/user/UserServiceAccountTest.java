package com.example.authpoc.user;

import com.example.authpoc.exception.ResourceNotFoundException;
import com.example.authpoc.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceAccountTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private User user;

    private UserService userService;

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder,
                clock
        );
    }

    @Test
    @DisplayName("getUser should map the entity to a UserResponse")
    void getUser_ShouldMapToResponse() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(id);
        when(user.getUsername()).thenReturn("john");
        when(user.getEmail()).thenReturn("john@example.com");
        when(user.getRole()).thenReturn(Role.ADMIN);

        UserResponse response = userService.getUser(id);

        assertThat(response).isEqualTo(
                new UserResponse(id, "john", "john@example.com", Role.ADMIN)
        );
    }

    @Test
    @DisplayName("createUser should save the user with the encoded password and the default role")
    void createUser_ShouldHashPasswordAndSave() {
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.createUser("john", "john@example.com", "pw");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded");
        assertThat(captor.getValue().getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("createUser should rely on the database constraint for duplicate usernames")
    void createUser_DuplicateUsername_ShouldPropagateConstraintViolation() {
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate username"));

        assertThatThrownBy(() -> userService.createUser("john", "john@example.com", "pw"))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("createUser should rely on the database constraint for duplicate emails")
    void createUser_DuplicateEmail_ShouldPropagateConstraintViolation() {
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        assertThatThrownBy(() -> userService.createUser("john", "john@example.com", "pw"))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("updateUserStatus should set the enabled flag on the user")
    void updateUserStatus_ShouldSetEnabled() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.updateUserStatus(id, false);

        verify(user).setEnabled(false);
    }

    @Test
    @DisplayName("updateUserRole should set the role on the user")
    void updateUserRole_ShouldSetRole() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.updateUserRole(id, Role.ADMIN);

        verify(user).setRole(Role.ADMIN);
    }

    @Test
    @DisplayName("recordLogin should record the login on the user")
    void recordLogin_ShouldDelegateToEntity() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.recordLogin(id);

        verify(user).recordLogin(NOW);
    }

    @Test
    @DisplayName("deleteUser should delete an existing user")
    void deleteUser_Existing_ShouldDelete() {
        when(userRepository.existsById(id)).thenReturn(true);

        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
    }

    @Test
    @DisplayName("deleteUser should remain successful when another request deletes the user after the existence check")
    void deleteUser_ConcurrentDelete_ShouldBeIdempotent() {
        when(userRepository.existsById(id)).thenReturn(true);

        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
    }

    @ParameterizedTest(name = "{0} should reject an unknown user")
    @EnumSource(AccountOperation.class)
    void unknownUser_ShouldThrowResourceNotFound(AccountOperation operation) {
        if (operation == AccountOperation.DELETE) {
            when(userRepository.existsById(id)).thenReturn(false);
        } else {
            when(userRepository.findById(id)).thenReturn(Optional.empty());
        }

        assertThatThrownBy(() -> execute(operation))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
        verify(userRepository, never()).save(any());
    }

    private void execute(AccountOperation operation) {
        switch (operation) {
            case GET -> userService.getUser(id);
            case UPDATE_STATUS -> userService.updateUserStatus(id, false);
            case UPDATE_ROLE -> userService.updateUserRole(id, Role.ADMIN);
            case RECORD_LOGIN -> userService.recordLogin(id);
            case DELETE -> userService.deleteUser(id);
        }
    }

    private enum AccountOperation {
        GET,
        UPDATE_STATUS,
        UPDATE_ROLE,
        RECORD_LOGIN,
        DELETE
    }
}
