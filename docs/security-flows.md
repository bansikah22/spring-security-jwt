# Security Flows

## Browser login and session

```mermaid
sequenceDiagram
    participant B as Browser
    participant S as Spring Security
    participant D as PostgreSQL
    B->>S: POST /login with CSRF token
    S->>D: Load user and password hash
    D-->>S: User roles and state
    S->>S: Verify password and account status
    S-->>B: Rotate session ID and redirect /dashboard
    B->>S: GET /admin
    S-->>B: 403 access-denied for USER, 200 for ADMIN
```

The login response is intentionally generic for unknown, disabled, locked, and invalid-password accounts. This avoids confirming whether a username exists.

## JWT issuance and API authorization

```mermaid
sequenceDiagram
    participant C as API client
    participant A as Auth API
    participant R as Resource server
    C->>A: POST /api/auth/token
    A-->>C: RSA-signed access token and random refresh token
    C->>R: GET protected API with Bearer token
    R->>R: Validate signature, expiry, not-before, issuer, audience
    R->>R: Map roles, permissions, and scopes to authorities
    R-->>C: 200, 401, or 403
```

Access tokens include only authorization claims: issuer, audience, subject, timestamps, JWT ID, roles, permissions, and scope. Never add credentials, refresh tokens, or private profile data to a signed JWT.

## Refresh token rotation

```mermaid
sequenceDiagram
    participant C as Client
    participant A as Auth API
    participant D as PostgreSQL
    C->>A: POST /api/auth/refresh with refresh token A
    A->>D: Hash and find token A
    A->>D: Revoke token A
    A->>D: Store hash of token B
    A-->>C: New access token and refresh token B
    C->>A: Reuse token A
    A-->>C: 401 Unauthorized
```

Only SHA-256 hashes of refresh tokens are stored. A refresh token is revoked before its replacement is returned, making it single-use.

## Test matrix

`SecurityIntegrationTest` verifies:

- Anonymous redirect, session logout CSRF protection, and session fixation behavior.
- Role-based web authorization and permission-based API authorization.
- Valid JWT authority mapping plus malformed, expired, wrong issuer, wrong audience, and invalid-signature rejection.
- Generic authentication failure for unknown, disabled, and locked accounts.
- Refresh-token replay rejection, CORS policy, static Security Lab assets, and health probes.