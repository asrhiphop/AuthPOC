package com.example.authpoc.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordConstraintValidatorTest {

    private PasswordConstraintValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Captor
    private ArgumentCaptor<String> errorCodeCaptor;

    @BeforeEach
    void setUp() {
        validator = new PasswordConstraintValidator();
    }

    @Test
    @DisplayName("Should return false when password is null")
    void isValid_WithNull_ShouldReturnFalse() {
        boolean result = validator.isValid(null, context);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true when password meets all requirements")
    void isValid_WithValidPassword_ShouldReturnTrue() {
        boolean result = validator.isValid("SecureP@ss123", context);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false and register error codes when password fails complexity rules")
    void isValid_WithWeakPassword_ShouldReturnFalseAndRegisterCodes() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

        boolean result = validator.isValid("pass", context);

        assertThat(result).isFalse();

        verify(context).disableDefaultConstraintViolation();

        verify(context, times(4)).buildConstraintViolationWithTemplate(errorCodeCaptor.capture());

        List<String> capturedCodes = errorCodeCaptor.getAllValues();
        assertThat(capturedCodes).containsExactlyInAnyOrder(
                "TOO_SHORT",
                "INSUFFICIENT_UPPERCASE",
                "INSUFFICIENT_DIGIT",
                "INSUFFICIENT_SPECIAL"
        );
    }
}