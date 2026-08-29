package com.bansikah.secureportal.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security.jwt.keys")
public record JwtKeyProperties(String publicKeyLocation, String privateKeyLocation) {
}