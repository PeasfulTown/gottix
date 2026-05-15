package xyz.peasfultown.gottix.auth_service.service;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xyz.peasfultown.gottix.auth_service.exception.*;
import xyz.peasfultown.gottix.auth_service.model.LoginRequest;
import xyz.peasfultown.gottix.auth_service.model.RefreshToken;
import xyz.peasfultown.gottix.auth_service.model.RegisterRequest;
import xyz.peasfultown.gottix.auth_service.model.Token;
import xyz.peasfultown.gottix.auth_service.entity.RefreshTokenEntity;
import xyz.peasfultown.gottix.auth_service.entity.UserEntity;
import xyz.peasfultown.gottix.auth_service.repository.RefreshTokenRepository;
import xyz.peasfultown.gottix.auth_service.repository.UserRepository;
import xyz.peasfultown.gottix.auth_service.utils.JwtUtil;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlocklistService blocklistService;


    @Override
    public Token login(LoginRequest loginRequest) {
        UserEntity ue = userRepo.findUserByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException(String.format(
                        "User not found by email: %s", loginRequest.getEmail()
                )));
        if (!passwordEncoder.matches(loginRequest.getPassword(), ue.getPassword()))
            throw new UnauthorizedException(String.format(
                    "Password incorrect for user email: %s", loginRequest.getEmail()
            ));

        return createTokens(ue);
    }

    @Override
    public void logout(Token token) {
        refreshTokenRepository.findByToken(UUID.fromString(token.getRefreshToken()))
                .ifPresent(t -> {
                    t.setRevoked(true);
                    refreshTokenRepository.save(t);
                });
        Claims claims = jwtUtil.getClaimsFromToken(token.getAccessToken());
        String jti = claims.getId();
        Date expiry = claims.getExpiration();
        blocklistService.block(jti, expiry);
    }

    @Override
    public Token register(RegisterRequest registerRequest) {
        userRepo.findUserByEmail(registerRequest.getEmail())
                .ifPresent(ue -> {
                    throw new UserAlreadyExistsException(String.format(
                            "unable to register user, user already exists by email: %s", ue.getEmail()
                    ));
                });

        UserEntity ue = UserEntity.builder()
                .id(UUID.randomUUID())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .build();

        userRepo.save(ue);
        return createTokens(ue);
    }

    @Override
    public Token getNewAccessToken(RefreshToken refreshToken) {
        RefreshTokenEntity rte = refreshTokenRepository.findByToken(UUID.fromString(refreshToken.getToken()))
                .orElseThrow(() -> new InvalidTokenException(String.format(
                        "refresh token not found by token: %s", refreshToken.getToken()
                )));
        if (rte.isRevoked() || rte.getExpiresAt().isBefore(Instant.now()))
            throw new InvalidTokenException("use of invalid refresh token (already revoked or expired)");
        rte.setRevoked(true);
        refreshTokenRepository.save(rte);
        return createTokens(rte.getUser());
    }

    private Token createTokens(UserEntity ue) {
        String accessToken = jwtUtil.generateAccessToken(ue);
        String refreshToken = refreshTokenService.createRefreshToken(ue).getToken().toString();
        return Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
