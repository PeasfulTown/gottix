package xyz.peasfultown.gottix.auth_service.service;

import xyz.peasfultown.gottix.auth_service.model.LoginRequest;
import xyz.peasfultown.gottix.auth_service.model.RefreshToken;
import xyz.peasfultown.gottix.auth_service.model.RegisterRequest;
import xyz.peasfultown.gottix.auth_service.model.Token;

public interface AuthService {
    Token login(LoginRequest loginRequest);

    void logout(Token token);

    Token register(RegisterRequest registerRequest);

    Token getNewAccessToken(RefreshToken refreshToken);
}
