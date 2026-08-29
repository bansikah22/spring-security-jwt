# SecurePortal Learning Guide

SecurePortal is a runnable Spring Boot 4 and Java 25 demonstration of two complementary Spring Security designs:

- Browser pages authenticate with form login, an HTTP session, CSRF protection, and session fixation protection.
- REST APIs authenticate with RSA-signed JWT bearer tokens, using Spring Security Resource Server validation.

## Start here

1. Run the portal locally and sign in as `user`, `manager`, or `admin`.
2. Open **Security context** to compare application roles and permissions.
3. Open **Security lab** to issue a development token and observe `401`, `403`, and successful API responses.
4. Read the [architecture](architecture.md) and [security flows](security-flows.md) while testing each scenario.

## Demonstration accounts

| Account | Password | Purpose |
| --- | --- | --- |
| `user` | `change-me-user` | Demonstrates permission denial for report generation and administration. |
| `manager` | Configure locally if needed | Demonstrates report generation permission without administration permission. |
| `admin` | `change-me-admin` | Demonstrates role-based web administration and permission-based API administration. |

Development credentials are intentionally local-only demonstration data. Never enable them in a deployed environment.

## Learning outcomes

The project shows how Spring Security handles authentication, authorization, password hashing, CSRF, sessions, JWT validation, authority mapping, refresh-token rotation, CORS, headers, security testing, container builds, and Kubernetes deployment.