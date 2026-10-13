package com.example.authpoc.auth.token;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TokenRevocationService {

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenRevocationService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeFamily(UUID familyId) {
        refreshTokenRepository.deleteAllByFamilyId(familyId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteExpiredToken(UUID tokenId) {
        refreshTokenRepository.deleteById(tokenId);
    }
}
