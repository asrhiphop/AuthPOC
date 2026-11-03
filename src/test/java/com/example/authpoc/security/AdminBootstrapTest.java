package com.example.authpoc.security;

import com.example.authpoc.security.config.AdminProperties;
import com.example.authpoc.user.Role;
import com.example.authpoc.user.User;
import com.example.authpoc.user.UserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminBootstrapTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ApplicationArguments arguments = mock(ApplicationArguments.class);

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
    }

    @Test
    void validConfiguration_ShouldCreateAdmin() throws Exception {
        AdminProperties properties = new AdminProperties(
                "admin",
                "admin@example.com",
                "SecureP@ss123!"
        );
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        new AdminBootstrap(userRepository, passwordEncoder, properties, validator).run(arguments);

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("admin");
        assertThat(captor.getValue().getEmail()).isEqualTo("admin@example.com");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void missingConfiguration_ShouldFailWithoutQueryingDatabase() {
        AdminProperties properties = new AdminProperties(
                "",
                "admin@example.com",
                "SecureP@ss123!"
        );

        assertThatThrownBy(() -> new AdminBootstrap(
                userRepository,
                passwordEncoder,
                properties,
                validator
        ).run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("username")
                .hasMessageNotContaining("SecureP@ss123!");

        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void weakPassword_ShouldFailWithoutExposingPassword() {
        AdminProperties properties = new AdminProperties(
                "admin",
                "admin@example.com",
                "weak-password"
        );

        assertThatThrownBy(() -> new AdminBootstrap(
                userRepository,
                passwordEncoder,
                properties,
                validator
        ).run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("password")
                .hasMessageNotContaining("weak-password");

        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void existingNonAdminUser_ShouldFailClearly() {
        AdminProperties properties = new AdminProperties(
                "admin",
                "admin@example.com",
                "SecureP@ss123!"
        );
        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(new User("admin", "user@example.com", "hash", Role.USER)));

        assertThatThrownBy(() -> new AdminBootstrap(
                userRepository,
                passwordEncoder,
                properties,
                validator
        ).run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-admin")
                .hasMessageNotContaining("SecureP@ss123!");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void run_ShouldBeTransactional() throws NoSuchMethodException {
        assertThat(AdminBootstrap.class
                .getMethod("run", ApplicationArguments.class)
                .getAnnotation(Transactional.class))
                .isNotNull();
    }
}
