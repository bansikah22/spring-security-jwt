/**
 * Defines API origins permitted to make browser requests.
 */
package com.bansikah.secureportal.security.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security.cors")
public record CorsProperties(List<String> allowedOrigins) {
}