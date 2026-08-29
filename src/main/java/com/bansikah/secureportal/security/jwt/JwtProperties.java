package com.bansikah.secureportal.security.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security.jwt")
public record JwtProperties(String issuer, String audience, Duration accessTokenTtl, Duration refreshTokenTtl) {
}