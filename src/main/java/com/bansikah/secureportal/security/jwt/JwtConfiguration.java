package com.bansikah.secureportal.security.jwt;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;

@Configuration
@Profile({"dev", "test"})
class JwtConfiguration {

    @Bean
    KeyPair jwtKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("RSA is unavailable", exception);
        }
    }

    @Bean
    JwtEncoder jwtEncoder(KeyPair jwtKeyPair) {
        RSAKey jwk = new RSAKey.Builder((RSAPublicKey) jwtKeyPair.getPublic())
                .privateKey(jwtKeyPair.getPrivate())
                .keyID("secure-portal-development")
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new com.nimbusds.jose.jwk.JWKSet(jwk)));
    }

    @Bean
    JwtDecoder jwtDecoder(KeyPair jwtKeyPair, JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) jwtKeyPair.getPublic()).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()), audienceValidator(properties.audience())));
        return decoder;
    }

    private OAuth2TokenValidator<Jwt> audienceValidator(String audience) {
        OAuth2Error error = new OAuth2Error("invalid_token", "The token audience is invalid", null);
        return token -> token.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(error);
    }
}

@Configuration
@Profile("prod")
@EnableConfigurationProperties(JwtKeyProperties.class)
class ProductionJwtConfiguration {

    @Bean
    JwtEncoder jwtEncoder(JwtKeyProperties properties, ResourceLoader resourceLoader) {
        RSAPublicKey publicKey = publicKey(properties, resourceLoader);
        RSAKey jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey(properties, resourceLoader))
                .keyID("secure-portal-production")
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new com.nimbusds.jose.jwk.JWKSet(jwk)));
    }

    @Bean
    JwtDecoder jwtDecoder(JwtKeyProperties keyProperties, JwtProperties properties, ResourceLoader resourceLoader) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey(keyProperties, resourceLoader)).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()), audienceValidator(properties.audience())));
        return decoder;
    }

    private RSAPublicKey publicKey(JwtKeyProperties properties, ResourceLoader resourceLoader) {
        try (var input = resourceLoader.getResource(properties.publicKeyLocation()).getInputStream()) {
            return RsaKeyConverters.x509().convert(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read configured JWT public key", exception);
        }
    }

    private java.security.interfaces.RSAPrivateKey privateKey(JwtKeyProperties properties, ResourceLoader resourceLoader) {
        try (var input = resourceLoader.getResource(properties.privateKeyLocation()).getInputStream()) {
            return RsaKeyConverters.pkcs8().convert(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read configured JWT private key", exception);
        }
    }

    private OAuth2TokenValidator<Jwt> audienceValidator(String audience) {
        OAuth2Error error = new OAuth2Error("invalid_token", "The token audience is invalid", null);
        return token -> token.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(error);
    }
}