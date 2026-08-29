# SecurePortal

Copyright (c) 2026 Bansikah. Licensed under the terms in [LICENSE](LICENSE).

A Spring Boot 4 and Java 25 demonstration of session-based browser security and JWT bearer authentication. Application packages use `com.bansikah.secureportal`.

## Run locally

Start PostgreSQL:

```shell
docker compose up -d
```

Run the development profile with Maven 4:

```shell
mvn spring-boot:run
```

Open `http://localhost:58081`. Development users are `user` / `change-me-user` and `admin` / `change-me-admin`; replace these immediately outside a local demonstration environment.

## Deployment configuration

The default values make local development work. Deployments can override them without changing the artifact:

| Variable | Purpose | Example |
| --- | --- | --- |
| `SERVER_PORT` | HTTP listening port | `8080` |
| `JWT_ISSUER` | HTTPS issuer identifier for access tokens | `https://portal.example.com` |
| `JWT_AUDIENCE` | Intended JWT audience | `secure-portal-api` |
| `JWT_ACCESS_TOKEN_TTL` | ISO-8601 access-token lifetime | `PT15M` |
| `JWT_REFRESH_TOKEN_TTL` | ISO-8601 refresh-token lifetime | `P7D` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated browser origins allowed to call the API | `https://portal.example.com` |
| `DATABASE_URL` | JDBC connection URL | `jdbc:postgresql://postgres:5432/secure_portal` |
| `DATABASE_USERNAME` | Database username | `secure_portal` |
| `DATABASE_PASSWORD` | Database password, injected as a secret | `change-this` |

For a domain behind an ingress or reverse proxy, set `CORS_ALLOWED_ORIGINS` to its HTTPS origin and terminate TLS at the ingress. Kubernetes should inject database credentials and future signing-key locations through Secrets; do not place them in a ConfigMap or image.

## Container image

The multi-stage [Dockerfile](Dockerfile) uses exact Eclipse Temurin `25.0.4_7` JDK/JRE Noble image tags, builds with Apache Maven `4.0.0-rc-6`, verifies the Maven distribution SHA-512 checksum, and runs the application as an unprivileged user. No secrets are copied into the image.

Build the image:

```shell
docker build --tag secure-portal:0.0.1 .
```

Run it against a reachable PostgreSQL service. The container defaults to port `8080`; configure its public port and trusted browser domain through environment variables:

```shell
docker run --rm --publish 58081:8080 \
	--env SERVER_PORT=8080 \
	--env DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/secure_portal \
	--env DATABASE_USERNAME=secure_portal \
	--env DATABASE_PASSWORD=change-this \
	--env JWT_ISSUER=https://portal.example.com \
	--env CORS_ALLOWED_ORIGINS=https://portal.example.com \
	secure-portal:0.0.1
```

## Security model

Browser routes use Spring Security form login, an HTTP session, CSRF protection, session-fixation protection, and an access-denied page. REST routes under `/api/**` are stateless and require bearer JWTs, except `/api/auth/token` and `/api/auth/refresh`.

JWTs are signed with an ephemeral RSA key in the `dev` and `test` profiles. They include issuer, audience, subject, issued-at, not-before, expiry, JWT ID, roles, permissions, and scopes. Spring Security's resource-server support performs signature, expiry, not-before, issuer, and audience validation; roles map to `ROLE_*`, permissions map directly, and scopes map to `SCOPE_*`.

Refresh tokens are random credentials stored only as SHA-256 hashes. Refreshing revokes the presented token and issues a replacement, providing rotation and revocation without a JWT denylist. Access tokens stay short-lived.

The `prod` profile intentionally has no generated signing key. Configure a persistent, externally managed asymmetric key pair before deploying it; never commit a private key, database password, or refresh token.

## Tests

```shell
mvn test
```

The MockMvc suite verifies redirect authentication, role restrictions, CSRF logout protection, missing bearer-token rejection, role-to-token authority mapping, request and method-level permission checks, CORS policy, refresh-token rotation, and token issuance.
