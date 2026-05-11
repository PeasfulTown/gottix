package xyz.peasfultown.gottix.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.peasfultown.gottix.auth_service.entity.RefreshTokenEntity;
import xyz.peasfultown.gottix.auth_service.entity.UserEntity;
import xyz.peasfultown.gottix.auth_service.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${jwt.refresh-expiration-ms}")
    private long expiry;

    @Override
    public RefreshTokenEntity createRefreshToken(UserEntity ue) {
        RefreshTokenEntity rte = RefreshTokenEntity.builder()
                .user(ue)
                .token(UUID.randomUUID())
                .expiresAt(Instant.now().plusMillis(expiry))
                .build();
        return refreshTokenRepository.save(rte);
    }

}
