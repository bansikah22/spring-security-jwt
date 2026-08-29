# JWT and API Security

JWT is used for the REST API, not for the browser session pages. A client sends the JWT in an `Authorization` header on every API request.

```http
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

## Get a development token

Use the **Security lab** page while signed in, or make a request directly:

```shell
curl --request POST http://localhost:58081/api/auth/token \
  --header 'Content-Type: application/json' \
  --data '{"username":"user","password":"change-me-user"}'
```

The response contains an access token and a refresh token. In production, do not log either value, put either in a URL, or paste either into an untrusted JWT decoder.

## What is inside the access token

The access token contains authorization information, not secrets:

```json
{
  "iss": "https://secure-portal.local",
  "sub": "user",
  "aud": ["secure-portal-api"],
  "exp": 1234567890,
  "jti": "unique-token-id",
  "roles": ["USER"],
  "permissions": ["PROFILE_READ", "REPORT_READ"],
  "scope": "profile.read report.read"
}
```

JWT payloads are encoded, not encrypted. Anyone holding a token can read the payload, so passwords, refresh tokens, and private customer data must never be added as claims.

## How validation works

`JwtDecoder` verifies the RSA signature before trusting claims. It also rejects tokens with the wrong issuer, wrong audience, expired time, invalid not-before time, malformed structure, or an untrusted signing key.

```mermaid
flowchart LR
    Header[Bearer token] --> Decoder[JwtDecoder]
    Decoder --> Signature{RSA signature valid?}
    Signature -- No --> Unauthorized[401 Unauthorized]
    Signature -- Yes --> Claims{Issuer, audience and time valid?}
    Claims -- No --> Unauthorized
    Claims -- Yes --> Authorities[Map roles and permissions]
    Authorities --> Rule{Authorized for endpoint?}
    Rule -- No --> Forbidden[403 Forbidden]
    Rule -- Yes --> API[Call API controller]
```

`401 Unauthorized` means the API cannot authenticate the request. `403 Forbidden` means the request is authenticated but lacks a required authority.

## Roles, permissions, and scopes

The application maps claims as follows:

| Token claim | Spring authority | Example |
| --- | --- | --- |
| `roles` | `ROLE_<value>` | `ADMIN` becomes `ROLE_ADMIN` |
| `permissions` | Direct authority | `ADMIN_READ` remains `ADMIN_READ` |
| `scope` | `SCOPE_<value>` | `profile.read` becomes `SCOPE_profile.read` |

For example, `/api/admin` requires `ADMIN_READ`, while `/api/security/reports` uses `@PreAuthorize("hasAuthority('REPORT_GENERATE')")`. The second rule is method security: it protects the controller method itself, not just the URL.

## Refresh-token rotation

Access tokens have a short lifetime. A refresh token requests a replacement access token:

```shell
curl --request POST http://localhost:58081/api/auth/refresh \
  --header 'Content-Type: application/json' \
  --data '{"refreshToken":"the-refresh-token"}'
```

Refresh tokens are random values. Only their SHA-256 hashes are stored in the database. On refresh, the original token is revoked and a new refresh token is created. Reusing the old token returns `401`, which limits damage from token replay.