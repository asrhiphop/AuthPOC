package com.example.authpoc.user;

import com.example.authpoc.exception.ResourceAlreadyExistsException;
import com.example.authpoc.exception.ResourceNotFoundException;
import com.example.authpoc.security.config.LockoutProperties;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
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
                new LockoutProperties(3, Duration.ofMinutes(15)),
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
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.createUser("john", "john@example.com", "pw");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded");
        assertThat(captor.getValue().getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("createUser should reject a duplicate username")
    void createUser_DuplicateUsername_ShouldThrow() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("john", "john@example.com", "pw"))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser should reject a duplicate email")
    void createUser_DuplicateEmail_ShouldThrow() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("john", "john@example.com", "pw"))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
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
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
    }

    @ParameterizedTest(name = "{0} should reject an unknown user")
    @EnumSource(AccountOperation.class)
    void unknownUser_ShouldThrowResourceNotFound(AccountOperation operation) {
        when(userRepository.findById(id)).thenReturn(Optional.empty());

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
