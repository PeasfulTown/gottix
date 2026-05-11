package xyz.peasfultown.gottix.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.auth_service.AuthApi;
import xyz.peasfultown.ecommerce.auth_service.model.LoginRequest;
import xyz.peasfultown.ecommerce.auth_service.model.RefreshToken;
import xyz.peasfultown.ecommerce.auth_service.model.RegisterRequest;
import xyz.peasfultown.ecommerce.auth_service.model.Token;
import xyz.peasfultown.gottix.auth_service.service.AuthService;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    private final AuthService authService;

    @Override
    public ResponseEntity<Token> login(LoginRequest loginRequest) throws Exception {
        return ok(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<Void> logout(Token token) throws Exception {
        authService.logout(token);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Token> register(RegisterRequest registerRequest) throws Exception {
        return status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @Override
    public ResponseEntity<Token> renewAccessToken(RefreshToken refreshToken) throws Exception {
        return ok(authService.getNewAccessToken(refreshToken));
    }
}
