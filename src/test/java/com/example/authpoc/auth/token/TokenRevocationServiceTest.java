package com.example.authpoc.auth.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenRevocationServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private TokenRevocationService service;

    @BeforeEach
    void setUp() {
        service = new TokenRevocationService(refreshTokenRepository);
    }

    @Test
    @DisplayName("revokeFamily should delete every token in the family")
    void revokeFamily_ShouldDeleteFamily() {
        UUID familyId = UUID.randomUUID();

        service.revokeFamily(familyId);

        verify(refreshTokenRepository).deleteAllByFamilyId(familyId);
    }

    @Test
    @DisplayName("deleteExpiredToken should delete the specified token")
    void deleteExpiredToken_ShouldDeleteToken() {
        UUID tokenId = UUID.randomUUID();

        service.deleteExpiredToken(tokenId);

        verify(refreshTokenRepository).deleteById(tokenId);
    }

    @Test
    @DisplayName("revocation methods should start independent transactions")
    void revocationMethods_ShouldUseRequiresNewTransactions() throws NoSuchMethodException {
        assertThat(transactionPropagation("revokeFamily"))
                .isEqualTo(Propagation.REQUIRES_NEW);
        assertThat(transactionPropagation("deleteExpiredToken"))
                .isEqualTo(Propagation.REQUIRES_NEW);
    }

    private Propagation transactionPropagation(String methodName) throws NoSuchMethodException {
        Method method = TokenRevocationService.class.getMethod(methodName, UUID.class);
        return method.getAnnotation(Transactional.class).propagation();
    }
}
