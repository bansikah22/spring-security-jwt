package com.bansikah.secureportal.auth.refresh;

import com.bansikah.secureportal.security.jwt.JwtProperties;
import com.bansikah.secureportal.user.domain.UserAccount;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final RefreshTokenRepository tokens;
    private final JwtProperties properties;

    public RefreshTokenService(RefreshTokenRepository tokens, JwtProperties properties) {
        this.tokens = tokens;
        this.properties = properties;
    }

    @Transactional
    public String issue(UserAccount account) {
        byte[] value = new byte[32];
        RANDOM.nextBytes(value);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(value);
        tokens.save(new RefreshToken(account, hash(rawToken), Instant.now().plus(properties.refreshTokenTtl())));
        return rawToken;
    }

    @Transactional
    public UserAccount rotate(String rawToken) {
        RefreshToken token = tokens.findByTokenHash(hash(rawToken))
                .filter(stored -> stored.isUsable(Instant.now()))
                .orElseThrow(InvalidRefreshTokenException::new);
        token.revoke(Instant.now());
        return token.getUser();
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public static class InvalidRefreshTokenException extends RuntimeException {
    }
}