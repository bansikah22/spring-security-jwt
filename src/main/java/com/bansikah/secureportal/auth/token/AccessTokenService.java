package com.bansikah.secureportal.auth.token;

import com.bansikah.secureportal.security.jwt.JwtProperties;
import com.bansikah.secureportal.user.domain.Permission;
import com.bansikah.secureportal.user.domain.Role;
import com.bansikah.secureportal.user.domain.UserAccount;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public AccessTokenService(JwtEncoder jwtEncoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public IssuedAccessToken issue(UserAccount account) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.accessTokenTtl());
        var permissions = account.getRoles().stream().flatMap(role -> role.permissions().stream())
                .map(Permission::name).distinct().sorted().toList();
        var scopes = permissions.stream().map(permission -> permission.toLowerCase().replace('_', '.')).toList();
        var roles = account.getRoles().stream().map(Role::name).sorted().toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(account.getUsername())
                .audience(java.util.List.of(properties.audience()))
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("scope", String.join(" ", scopes))
                .claim("roles", roles)
                .claim("permissions", permissions)
                .build();
        return new IssuedAccessToken(jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(), expiresAt);
    }

    public record IssuedAccessToken(String value, Instant expiresAt) {
    }
}