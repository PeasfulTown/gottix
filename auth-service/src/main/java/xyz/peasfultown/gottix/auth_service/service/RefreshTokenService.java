package xyz.peasfultown.gottix.auth_service.service;

import xyz.peasfultown.gottix.auth_service.entity.RefreshTokenEntity;
import xyz.peasfultown.gottix.auth_service.entity.UserEntity;

public interface RefreshTokenService {
    RefreshTokenEntity createRefreshToken(UserEntity ue);
}
