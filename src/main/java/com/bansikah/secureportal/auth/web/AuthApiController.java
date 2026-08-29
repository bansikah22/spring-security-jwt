package com.bansikah.secureportal.auth.web;

import com.bansikah.secureportal.auth.refresh.RefreshTokenService;
import com.bansikah.secureportal.auth.token.AccessTokenService;
import com.bansikah.secureportal.user.domain.UserAccount;
import com.bansikah.secureportal.user.repository.UserAccountRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository users;
    private final AccessTokenService accessTokens;
    private final RefreshTokenService refreshTokens;

    public AuthApiController(AuthenticationManager authenticationManager, UserAccountRepository users,
                             AccessTokenService accessTokens, RefreshTokenService refreshTokens) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.accessTokens = accessTokens;
        this.refreshTokens = refreshTokens;
    }

    @PostMapping("/token")
    public TokenResponse token(@Valid @RequestBody TokenRequest request) {
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        return tokensFor(user(request.username()));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        UserAccount account = refreshTokens.rotate(request.refreshToken());
        return tokensFor(account);
    }

    @ExceptionHandler({AuthenticationException.class, RefreshTokenService.InvalidRefreshTokenException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiError invalidCredentials() {
        return new ApiError(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "Authentication failed");
    }

    private UserAccount user(String username) {
        return users.findByUsername(username).orElseThrow();
    }

    private TokenResponse tokensFor(UserAccount account) {
        AccessTokenService.IssuedAccessToken accessToken = accessTokens.issue(account);
        return new TokenResponse(accessToken.value(), refreshTokens.issue(account), "Bearer", accessToken.expiresAt());
    }

    public record TokenRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record TokenResponse(String accessToken, String refreshToken, String tokenType, Instant expiresAt) {
    }

    public record ApiError(int status, String error, String message) {
    }
}