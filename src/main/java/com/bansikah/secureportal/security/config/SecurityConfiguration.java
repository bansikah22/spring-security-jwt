package com.bansikah.secureportal.security.config;

import com.bansikah.secureportal.security.jwt.JwtProperties;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpStatus;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
public class SecurityConfiguration {

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurity(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        return http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/token", "/api/auth/refresh").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN_READ")
                        .requestMatchers("/api/security/**").authenticated()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) -> response.sendError(HttpStatus.FORBIDDEN.value())))
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/access-denied", "/css/**", "/js/**", "/favicon.ico", "/actuator/health/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard", "/profile", "/security", "/security-lab").authenticated()
                        .anyRequest().denyAll())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/dashboard", true).permitAll())
                .logout(logout -> logout.logoutUrl("/logout")
                        .logoutSuccessUrl("/").invalidateHttpSession(true).clearAuthentication(true))
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied")
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .sessionManagement(session -> session.sessionFixation(fixation -> fixation.migrateSession()))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; style-src 'self'"))
                        .contentTypeOptions(Customizer.withDefaults())
                        .referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicyHeader(policy -> policy.policy("geolocation=(), microphone=(), camera=()")))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = new java.util.LinkedHashSet<org.springframework.security.core.GrantedAuthority>();
            String scope = jwt.getClaimAsString("scope");
            if (scope != null) {
                for (String value : scope.split(" ")) {
                    authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("SCOPE_" + value));
                }
            }
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.forEach(role -> authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)));
            }
            List<String> permissions = jwt.getClaimAsStringList("permissions");
            if (permissions != null) {
                permissions.forEach(permission -> authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(permission)));
            }
            return authorities;
        });
        return converter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-CSRF-TOKEN"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}