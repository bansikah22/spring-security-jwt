package com.bansikah.secureportal.security.web;

import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
public class SecurityApiController {

    @GetMapping("/me")
    Map<String, Object> currentToken(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "subject", jwt.getSubject(),
                "issuer", jwt.getIssuer().toString(),
                "roles", jwt.getClaimAsStringList("roles"),
                "permissions", jwt.getClaimAsStringList("permissions"));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    Map<String, String> generateReport() {
        return Map.of("status", "Report generation is authorized");
    }
}