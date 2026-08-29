package com.bansikah.secureportal;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.bansikah.secureportal.user.domain.Role;
import com.bansikah.secureportal.user.domain.UserAccount;
import com.bansikah.secureportal.user.repository.UserAccountRepository;
import com.bansikah.secureportal.security.jwt.JwtProperties;
import com.bansikah.secureportal.auth.refresh.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.stream.Stream;
import java.time.Instant;
import java.security.KeyPairGenerator;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private RefreshTokenRepository refreshTokens;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        refreshTokens.deleteAll();
        users.deleteAll();
        users.save(new UserAccount("user", "user@example.test", passwordEncoder.encode("password"), Set.of(Role.USER)));
        users.save(new UserAccount("manager", "manager@example.test", passwordEncoder.encode("password"), Set.of(Role.MANAGER)));
        users.save(new UserAccount("admin", "admin@example.test", passwordEncoder.encode("password"), Set.of(Role.ADMIN)));
    }

    @Test
    void anonymousDashboardRedirectsToLogin() throws Exception {
        mvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void securityLabJavaScriptIsPubliclyServed() throws Exception {
        mvc.perform(get("/js/security-lab.js"))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotAccessAdminPage() throws Exception {
        mvc.perform(get("/admin").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminPage() throws Exception {
        mvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

        @Test
        void formLoginCreatesSessionThatIsDeniedAdminAccessForNormalUser() throws Exception {
        var login = mvc.perform(get("/login")).andReturn();
            var initialSession = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession();
            var loginResult = mvc.perform(post("/login").session(initialSession)
                .with(csrf()).param("username", "user").param("password", "password"))
            .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn();
            var authenticatedSession = (org.springframework.mock.web.MockHttpSession) loginResult.getRequest().getSession(false);
            mvc.perform(get("/admin").session(authenticatedSession))
            .andExpect(status().isForbidden());
        }

    @Test
    void logoutRequiresCsrfToken() throws Exception {
        mvc.perform(post("/logout").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/logout").with(user("user").roles("USER")).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void apiRejectsMissingBearerToken() throws Exception {
        mvc.perform(get("/api/security/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void methodSecurityRequiresReportGeneratePermission() throws Exception {
        mvc.perform(get("/api/security/reports").with(jwt()
                        .authorities(new SimpleGrantedAuthority("REPORT_READ"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/security/reports").with(jwt()
                        .authorities(new SimpleGrantedAuthority("REPORT_GENERATE"))))
                .andExpect(status().isOk());
    }

    @Test
    void tokenEndpointIssuesAccessAndRefreshTokens() throws Exception {
        mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void unknownDisabledAndLockedAccountsAreRejectedWithoutAccountDisclosure() throws Exception {
        UserAccount disabled = new UserAccount("disabled", "disabled@example.test", passwordEncoder.encode("password"), Set.of(Role.USER));
        disabled.disable();
        users.save(disabled);
        UserAccount locked = new UserAccount("locked", "locked@example.test", passwordEncoder.encode("password"), Set.of(Role.USER));
        locked.lock();
        users.save(locked);

        for (String username : Set.of("unknown", "disabled", "locked")) {
            mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"" + username + "\",\"password\":\"password\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Authentication failed"));
        }
    }

    @Test
    void decoderRejectsExpiredWrongIssuerAndWrongAudienceTokens() {
        Instant now = Instant.now();
        assertRejected(token(jwtEncoder, "https://invalid.example", jwtProperties.audience(), now, now.plusSeconds(60)));
        assertRejected(token(jwtEncoder, jwtProperties.issuer(), "wrong-audience", now, now.plusSeconds(60)));
        assertRejected(token(jwtEncoder, jwtProperties.issuer(), jwtProperties.audience(), now.minusSeconds(120), now.minusSeconds(60)));
    }

    @Test
    void decoderRejectsMalformedAndWrongSignatureTokens() throws Exception {
        assertRejected("not-a-jwt");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        var keyPair = keyPairGenerator.generateKeyPair();
        RSAKey jwk = new RSAKey.Builder((java.security.interfaces.RSAPublicKey) keyPair.getPublic()).privateKey(keyPair.getPrivate()).build();
        JwtEncoder untrustedEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(jwk)));
        assertRejected(token(untrustedEncoder, jwtProperties.issuer(), jwtProperties.audience(), Instant.now(), Instant.now().plusSeconds(60)));
    }

            @ParameterizedTest
            @MethodSource("roleClaims")
            void accessTokenContainsTheAssignedRoleAndPermissions(String username, String expectedRole, String expectedPermission) throws Exception {
            String response = mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"" + username + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
            JsonNode accessToken = objectMapper.readTree(response).get("accessToken");
            mvc.perform(get("/api/security/me").header("Authorization", "Bearer " + accessToken.asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.hasItem(expectedRole)))
                .andExpect(jsonPath("$.permissions").value(org.hamcrest.Matchers.hasItem(expectedPermission)));
            }

            @Test
            void apiAdminRouteRequiresAdminReadPermission() throws Exception {
            mvc.perform(get("/api/admin").with(jwt().authorities(new SimpleGrantedAuthority("USER_READ"))))
                .andExpect(status().isForbidden());
            mvc.perform(get("/api/admin").with(jwt().authorities(new SimpleGrantedAuthority("ADMIN_READ"))))
                .andExpect(status().isOk());
            }

            @Test
            void apiCorsPolicyAllowsOnlyConfiguredOrigin() throws Exception {
            mvc.perform(options("/api/security/me").header("Origin", "http://localhost:58081")
                    .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                    .string("Access-Control-Allow-Origin", "http://localhost:58081"));
            mvc.perform(options("/api/security/me").header("Origin", "https://untrusted.example")
                    .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
            }

            @Test
            void refreshTokenCanBeUsedOnlyOnce() throws Exception {
            String initialResponse = mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"user\",\"password\":\"password\"}"))
                .andReturn().getResponse().getContentAsString();
            String refreshToken = objectMapper.readTree(initialResponse).get("refreshToken").asText();
            String request = "{\"refreshToken\":\"" + refreshToken + "\"}";
            mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
            mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isUnauthorized());
            }

            private static Stream<Arguments> roleClaims() {
            return Stream.of(
                Arguments.of("user", "USER", "PROFILE_READ"),
                Arguments.of("manager", "MANAGER", "REPORT_GENERATE"),
                Arguments.of("admin", "ADMIN", "ADMIN_READ"));
            }

    private String token(JwtEncoder encoder, String issuer, String audience, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).subject("user").audience(java.util.List.of(audience))
                .issuedAt(issuedAt).notBefore(issuedAt).expiresAt(expiresAt).build();
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private void assertRejected(String token) {
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.security.oauth2.jwt.JwtException.class,
                () -> jwtDecoder.decode(token));
    }
}