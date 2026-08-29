# Testing Security

Security is only useful when authorization rules are tested as behavior. SecurePortal uses Spring Boot Test, MockMvc, and Spring Security Test in `SecurityIntegrationTest`.

## Run the tests

Spring Boot 4 currently requires Maven 4 for this project.

```shell
mvn clean test
```

The test profile uses an in-memory H2 database, so tests do not alter the local PostgreSQL database.

## What the tests prove

| Scenario | Expected result | Why it matters |
| --- | --- | --- |
| Anonymous request to `/dashboard` | Redirect to `/login` | Browser pages require authentication. |
| `USER` request to `/admin` | `403` | Route role checks are enforced by the server. |
| `ADMIN` request to `/admin` | `200` | The allowed role can use the route. |
| Logout without CSRF token | `403` | Cookie-backed actions require CSRF protection. |
| Missing bearer token | `401` | APIs reject unauthenticated clients. |
| Missing `REPORT_GENERATE` | `403` | Method-level permission enforcement works. |
| Wrong issuer, audience, signature, or expiry | JWT rejection | Token validation is more than decoding a payload. |
| Reused refresh token | `401` | Refresh rotation blocks replay. |
| Liveness and readiness endpoints | `200` | Kubernetes can probe the application safely. |

## Adding a new authorization test

When you add a protected endpoint, test both sides of the rule:

```java
mvc.perform(get("/api/example").with(jwt()
        .authorities(new SimpleGrantedAuthority("EXAMPLE_READ"))))
    .andExpect(status().isOk());

mvc.perform(get("/api/example").with(jwt()
        .authorities(new SimpleGrantedAuthority("PROFILE_READ"))))
    .andExpect(status().isForbidden());
```

The first request proves a permitted user can use the feature. The second guards against accidentally weakening access control later.

## CI checks

The Continuous Integration workflow runs tests, builds the container image, and scans it with Trivy. Dependabot opens weekly updates for Maven dependencies and GitHub Actions. A high or critical vulnerability with a known fix causes the scan job to fail, which is how the PostgreSQL JDBC driver update was detected.