# Spring Security JWT Showcase

## Project Implementation Blueprint

## 1. Project Overview

This project is a demonstration application designed to showcase modern Spring Security features using:

* Java 25
* Spring Boot
* Spring Security
* JWT
* Spring MVC
* Thymeleaf HTML templates
* PostgreSQL
* Maven
* Docker Compose

The goal is not simply to build another JWT login application.

The project will demonstrate how modern Spring Security features work together in a realistic application while following official Spring documentation and recommended practices as closely as possible.

The application will demonstrate both traditional browser authentication and JWT-based API authentication.

---

# 2. Main Project Goals

The application should demonstrate:

* Modern Spring Security configuration
* `SecurityFilterChain`
* Form-based authentication
* Session-based authentication
* JWT authentication
* JWT issuance
* JWT validation
* JWT claims
* JWT scopes and authorities
* Role-based authorization
* Permission-based authorization
* Request-level authorization
* Method-level authorization
* Secure password storage
* Authentication and authorization flows
* Access and refresh tokens
* Refresh token rotation
* Logout
* Token revocation strategy
* CSRF protection
* CORS
* Security headers
* Session security
* Authentication failure handling
* Authorization failure handling
* Security audit events
* Security testing

The project should act as both:

1. A working application.
2. A practical demonstration of modern Spring Security concepts.

---

# 3. Core Development Principle

The project will follow this principle:

> Prefer Spring Security's built-in mechanisms before writing custom security code.

We should avoid unnecessary custom implementations when Spring Security already provides an official solution.

Examples include avoiding unnecessary:

* Custom JWT parsing
* Manual JWT signature validation
* Legacy custom JWT filters when Resource Server support is more appropriate
* Manual password hashing
* Custom authentication logic when `AuthenticationManager` and Spring Security mechanisms can be used

Custom code should only be introduced when necessary.

---

# 4. Technology Stack

## Programming Language

```text
Java 25
```

## Framework

```text
Spring Boot
```

## Security

```text
Spring Security
```

Features expected to be used include:

* Servlet Security
* Form Login
* OAuth2 Resource Server
* JWT
* Method Security
* Password Encoding
* Authorization

## Web Framework

```text
Spring MVC
```

## Template Engine

```text
Thymeleaf
```

Thymeleaf will be used to build the application's HTML pages.

The application should also integrate Thymeleaf with Spring Security to conditionally display content based on authentication and authorization.

Example use cases:

* Show the current user's username.
* Show content only to authenticated users.
* Show administration links only to administrators.
* Hide restricted functionality.

## Database

```text
PostgreSQL
```

PostgreSQL will be the main development database.

An embedded database may optionally be used for testing.

## Build Tool

```text
Maven
```

## Local Infrastructure

```text
Docker Compose
```

Docker Compose can be used to run:

* PostgreSQL
* The Spring application if desired
* Supporting development services

---

# 5. Application Concept

## Application Name

Recommended name:

```text
SecurePortal
```

The application will be a security demonstration portal containing public pages, authenticated pages, administrative pages, and protected APIs.

---

# 6. Application Areas

The application will have two major security areas.

## Web Application

The web application will use:

```text
Thymeleaf
+
Session Authentication
```

Example:

```text
Browser
   |
   v
Login Page
   |
   v
Spring Security Form Login
   |
   v
Authentication
   |
   v
Session Created
   |
   v
Protected HTML Pages
```

---

## REST API

The REST API will use:

```text
JWT Bearer Authentication
```

Example:

```text
Client
   |
   v
Authorization Header
Bearer JWT
   |
   v
Spring Security JWT Validation
   |
   v
Authenticated Request
   |
   v
Protected API Resource
```

---

# 7. Hybrid Security Architecture

The application should demonstrate that different parts of one application can use different authentication mechanisms.

Conceptually:

```text
                     Client
                        |
          +-------------+-------------+
          |                           |
          v                           v
     HTML Pages                    REST API
          |                           |
          v                           v
       Session                        JWT
          |                           |
          +-------------+-------------+
                        |
                        v
                 Spring Security
                        |
          +-------------+-------------+
          |                           |
          v                           v
    Authentication                Authorization
          |
          v
       PostgreSQL
```

Example application areas:

```text
/web/**
```

Uses session authentication.

```text
/api/**
```

Uses JWT bearer authentication.

---

# 8. Application Pages

## Public Pages

```text
/
```

Public landing page.

```text
/login
```

Login page.

```text
/access-denied
```

Access denied page.

---

## Authenticated Pages

```text
/dashboard
```

User dashboard.

```text
/profile
```

User profile.

```text
/security
```

Security demonstration dashboard.

This page can display safe information about the current authentication.

Examples:

```text
Authenticated User

Authentication Type

Granted Authorities

Roles

Permissions

Authentication Status
```

Sensitive secrets or private credentials must never be exposed.

---

## Administrative Pages

```text
/admin
```

Administration dashboard.

Potential functionality:

* View users.
* View roles.
* View permissions.
* View audit events.
* Demonstrate authorization restrictions.

---

# 9. REST API

Example API structure:

```text
/api/auth/**
```

Authentication and token operations.

```text
/api/users/**
```

User operations.

```text
/api/admin/**
```

Administrative operations.

```text
/api/security/**
```

Security demonstration endpoints.

---

# 10. Domain Model

The initial domain model may contain:

```text
User
Role
Permission
RefreshToken
SecurityAuditEvent
```

---

# 11. User Entity

Possible fields:

```text
id

username

email

password

enabled

accountLocked

createdAt

updatedAt
```

A user can have one or more roles.

Conceptually:

```text
User
  |
  +---- Role
  |
  +---- Role
```

---

# 12. Role Entity

Example roles:

```text
ROLE_USER

ROLE_MANAGER

ROLE_ADMIN
```

A role can contain permissions.

Example:

```text
ROLE_ADMIN
    |
    +---- USER_READ
    +---- USER_WRITE
    +---- USER_DELETE
    +---- ADMIN_READ
```

---

# 13. Permission Entity

Example permissions:

```text
USER_READ

USER_WRITE

USER_DELETE

PROFILE_READ

PROFILE_WRITE

REPORT_READ

REPORT_GENERATE

ADMIN_READ

ADMIN_WRITE
```

The project should demonstrate the difference between:

```text
Roles
```

and:

```text
Permissions / Authorities
```

---

# 14. Authentication Architecture

The application will demonstrate two authentication mechanisms.

## Session Authentication

Used by HTML pages.

Flow:

```text
User
 |
 v
Login Page
 |
 v
Username + Password
 |
 v
AuthenticationManager
 |
 v
AuthenticationProvider
 |
 v
UserDetailsService
 |
 v
Password Verification
 |
 v
Authentication Successful
 |
 v
SecurityContext
 |
 v
Session Created
```

---

## JWT Authentication

Used by REST APIs.

Flow:

```text
Client
 |
 v
JWT Bearer Token
 |
 v
Spring Security Resource Server
 |
 v
JWT Signature Validation
 |
 v
JWT Claims Validation
 |
 v
Authentication Created
 |
 v
SecurityContext
 |
 v
Protected API Access
```

---

# 15. Password Security

Passwords must never be stored in plain text.

The project will use Spring Security's password encoding infrastructure.

Example architecture:

```text
User Password
      |
      v
PasswordEncoder
      |
      v
Secure Password Hash
      |
      v
Database
```

During authentication:

```text
Submitted Password
      |
      v
PasswordEncoder.matches(...)
      |
      v
Stored Password Hash
```

The application will demonstrate:

* Password hashing.
* Password verification.
* Secure password changes.
* Password upgrade capability where applicable.

We will not implement cryptographic password hashing manually.

---

# 16. Spring Security Configuration

The project will use modern component-based configuration.

The application should avoid obsolete security configuration approaches.

The main configuration will use:

```text
SecurityFilterChain
```

Conceptually:

```text
HTTP Request
    |
    v
Security Filter Chain
    |
    +---- Authentication
    |
    +---- Authorization
    |
    +---- CSRF
    |
    +---- Security Context
    |
    +---- Exception Handling
    |
    v
Controller
```

---

# 17. URL Authorization

Examples of URL security:

```text
/                       -> Public

/login                  -> Public

/css/**                 -> Public

/js/**                  -> Public

/dashboard              -> Authenticated

/profile                -> Authenticated

/security               -> Authenticated

/admin/**               -> ROLE_ADMIN

/api/**                 -> JWT Authenticated
```

The project should demonstrate:

* `permitAll`
* `authenticated`
* `hasRole`
* `hasAnyRole`
* `hasAuthority`

---

# 18. Method-Level Security

The project will enable method security.

This allows authorization rules directly on application methods.

Examples of concepts to demonstrate:

```text
@PreAuthorize
```

Potential example:

```java
@PreAuthorize("hasAuthority('REPORT_GENERATE')")
```

The project should demonstrate the difference between:

```text
Request-Level Authorization
```

and:

```text
Method-Level Authorization
```

Request-level authorization protects URLs.

Method-level authorization protects application functionality.

---

# 19. JWT Architecture

JWT functionality will use Spring Security infrastructure where possible.

We should avoid manually implementing cryptographic token validation.

JWT processing will include:

```text
JWT Creation
JWT Signing
JWT Validation
JWT Claim Validation
JWT Authentication Conversion
Authority Mapping
```

---

# 20. JWT Key Strategy

The project should preferably demonstrate asymmetric cryptography.

Conceptually:

```text
Private Key
    |
    v
Signs JWT
    |
    v
JWT
    |
    v
Public Key
    |
    v
Validates JWT
```

This provides a realistic demonstration of distributed security architecture.

Conceptually:

```text
Token Issuer
    |
    | Private Key
    v
Signed JWT
    |
    | Public Key
    v
Resource Server
```

Keys and secrets must not be committed to Git.

Development keys should be handled through secure local configuration.

Production keys should eventually be handled using appropriate secret or key management infrastructure.

---

# 21. JWT Claims

Example conceptual JWT claims:

```json
{
  "iss": "secure-portal",
  "sub": "username",
  "aud": ["secure-portal-api"],
  "iat": 1234567890,
  "nbf": 1234567890,
  "exp": 1234569690,
  "scope": "profile.read reports.read",
  "roles": ["USER"],
  "jti": "unique-token-id"
}
```

The project should explain and demonstrate:

## iss

Issuer.

## sub

Subject.

## aud

Audience.

## iat

Issued-at time.

## nbf

Not-before time.

## exp

Expiration time.

## scope

Granted scopes.

## roles

Application roles.

## jti

Unique token identifier.

---

# 22. JWT Validation

JWT validation should include:

```text
Signature Validation

Expiration Validation

Not-Before Validation

Issuer Validation

Audience Validation

Algorithm Restrictions
```

We should rely on Spring Security's JWT support for standard cryptographic and claim validation.

The application should not treat manual expiration checking as complete JWT validation.

---

# 23. JWT Authorities

The project should demonstrate how JWT claims become Spring Security authorities.

Example:

```text
JWT Scope:

profile.read
reports.read
```

May become:

```text
SCOPE_profile.read

SCOPE_reports.read
```

Application roles may also be mapped into:

```text
ROLE_USER

ROLE_ADMIN
```

The exact mapping strategy should be documented clearly.

---

# 24. Access Tokens

Access tokens should have relatively short lifetimes.

Example development strategy:

```text
Access Token
15 minutes
```

The actual configured lifetime should be externally configurable.

Access tokens should contain only necessary claims.

Sensitive data should never be placed inside JWTs merely because they are signed.

JWT payloads should be treated as readable by whoever possesses the token unless encryption is specifically implemented.

---

# 25. Refresh Tokens

Refresh tokens will be used to obtain new access tokens.

Conceptually:

```text
Access Token
Short Lifetime

Refresh Token
Longer Lifetime
```

Possible development configuration:

```text
Access Token:
15 minutes

Refresh Token:
7 days
```

The project should support:

* Refresh token persistence.
* Refresh token expiration.
* Refresh token revocation.
* Refresh token rotation.

Refresh tokens should be treated as sensitive credentials.

---

# 26. Refresh Token Rotation

The recommended conceptual flow:

```text
Refresh Token A
      |
      v
Request New Access Token
      |
      v
Refresh Token A Invalidated
      |
      v
Refresh Token B Created
      |
      v
New Access Token Created
```

This helps demonstrate token lifecycle management.

---

# 27. Logout Strategy

Session logout:

```text
Logout
   |
   v
Session Invalidated
```

JWT logout requires additional design considerations.

The application should demonstrate:

```text
Short-lived Access Token
```

and:

```text
Refresh Token Revocation
```

Potentially:

```text
Token Denylist
```

The project documentation should explain the advantages and disadvantages of maintaining a denylist.

The initial implementation should not unnecessarily complicate JWT logout if refresh token revocation and short access-token expiration already provide an acceptable strategy for the demonstration.

---

# 28. CSRF Protection

Because the application uses HTML templates and session authentication, CSRF protection is important.

The project should demonstrate the difference between:

```text
Session Authentication
+
Browser Cookies
```

and:

```text
Stateless JWT Bearer Authentication
```

The project should document that JWT does not automatically mean CSRF protection should always be disabled.

CSRF considerations depend on how credentials are transported.

Examples:

```text
Authorization Header
```

versus:

```text
Automatically Sent Authentication Cookies
```

The project should keep CSRF enabled for browser/session-based functionality unless there is a carefully justified reason not to.

---

# 29. CORS

The project should demonstrate CORS configuration.

Important concepts:

```text
Allowed Origins

Allowed Methods

Allowed Headers

Credentials
```

Wildcard configuration should not be used carelessly when credentials are involved.

CORS configuration should be explicit and environment-aware.

---

# 30. Security Headers

The project should demonstrate HTTP security headers.

Potential headers include:

```text
Content-Security-Policy

X-Content-Type-Options

Referrer-Policy

Permissions-Policy
```

HTTPS deployments may also use:

```text
Strict-Transport-Security
```

The exact configuration should be tested because an overly restrictive policy can break application functionality.

---

# 31. Session Security

The project should explore Spring Security session protection features.

Topics include:

```text
Session Fixation Protection

Session Invalidation

Concurrent Sessions

SecurityContext Persistence
```

The application should document how authentication is stored and restored for session-based requests.

---

# 32. Authentication Failure Handling

Authentication failures should be handled appropriately.

Examples:

```text
Invalid Username or Password

Disabled Account

Locked Account

Expired Credentials
```

For HTML pages:

```text
Redirect to Login Page
+
Safe Error Message
```

Sensitive information should not be revealed unnecessarily.

For example, the login page should generally avoid telling an attacker whether:

```text
The username exists
```

or:

```text
The password is incorrect
```

unless there is a deliberate and justified requirement.

---

# 33. Authorization Failure Handling

The project should clearly distinguish between:

```text
401 Unauthorized
```

and:

```text
403 Forbidden
```

Conceptually:

```text
Not Authenticated
      |
      v
401
```

```text
Authenticated
But Insufficient Permission
      |
      v
403
```

HTML pages may redirect or show an access-denied page.

REST APIs should return structured error responses.

Example:

```json
{
  "status": 403,
  "error": "ACCESS_DENIED",
  "message": "You do not have permission to access this resource"
}
```

---

# 34. Thymeleaf Security Integration

The HTML templates should demonstrate security-aware rendering.

Examples include:

* Display current authenticated user.
* Show login link to anonymous users.
* Show logout link to authenticated users.
* Show administrator navigation only to administrators.
* Show permissions-based actions.

The UI should not be considered the security boundary.

Even if a button is hidden:

```text
Backend authorization must still enforce permissions.
```

---

# 35. Security Demonstration Dashboard

A dedicated security dashboard should be created.

Example information:

```text
Current User

Authentication Type

Authenticated Status

Authorities

Roles

Permissions
```

For development demonstrations, the page may show safe authentication information.

Sensitive values such as:

```text
Passwords

Private Keys

Refresh Tokens

Secrets
```

must never be exposed.

The dashboard should help explain the Spring Security `Authentication` and `SecurityContext` concepts.

---

# 36. Security Audit Events

The application may record security-related events.

Examples:

```text
LOGIN_SUCCESS

LOGIN_FAILURE

LOGOUT

ACCESS_DENIED

TOKEN_REFRESH

REFRESH_TOKEN_REVOKED

PASSWORD_CHANGED
```

Possible audit fields:

```text
id

eventType

username

timestamp

ipAddress

details
```

Care must be taken not to log:

* Passwords
* JWT values
* Refresh tokens
* Secrets

---

# 37. Package Structure

The project should prefer feature-oriented organization.

Recommended structure:

```text
com.example.secureportal

├── config
│
├── security
│   ├── config
│   ├── jwt
│   ├── authentication
│   ├── authorization
│   └── events
│
├── auth
│   ├── web
│   ├── service
│   ├── token
│   └── refresh
│
├── user
│   ├── domain
│   ├── repository
│   ├── service
│   └── web
│
├── role
│
├── permission
│
├── admin
│   ├── web
│   └── service
│
├── audit
│
└── common
```

The package structure can evolve as the application grows.

The goal is to avoid one large global:

```text
controller
service
repository
entity
```

structure where unrelated application features become tightly mixed.

---

# 38. Resource Structure

Recommended structure:

```text
src/main/resources

├── templates
│   │
│   ├── layout
│   │   ├── base.html
│   │   └── navigation.html
│   │
│   ├── auth
│   │   ├── login.html
│   │   └── access-denied.html
│   │
│   ├── dashboard
│   │   └── dashboard.html
│   │
│   ├── profile
│   │   └── profile.html
│   │
│   ├── admin
│   │   └── admin.html
│   │
│   └── security
│       └── security-dashboard.html
│
├── static
│   ├── css
│   ├── js
│   └── images
│
├── application.yml
├── application-dev.yml
├── application-test.yml
└── application-prod.yml
```

---

# 39. Environment Configuration

The application should separate configuration by environment.

Example:

```text
application.yml
```

Common configuration.

```text
application-dev.yml
```

Development configuration.

```text
application-test.yml
```

Testing configuration.

```text
application-prod.yml
```

Production configuration.

Sensitive configuration should not be committed to Git.

Examples include:

```text
Database Passwords

Private Keys

JWT Signing Keys

Production Secrets
```

Environment variables or an appropriate secret-management system should be used.

---

# 40. Docker Compose

Docker Compose can initially provide:

```text
PostgreSQL
```

Potentially later:

```text
Application

Monitoring

Supporting Infrastructure
```

The development environment should be simple enough to start with:

```text
docker compose up -d
```

---

# 41. Testing Strategy

Security testing will be a major part of the project.

---

## Authentication Tests

Test cases:

```text
Valid Login

Invalid Password

Unknown User

Disabled User

Locked User
```

---

## Authorization Tests

Examples:

```text
USER Cannot Access ADMIN Page

ADMIN Can Access ADMIN Page

USER Cannot Access ADMIN API

USER With Permission Can Access Protected Resource
```

---

## JWT Tests

Examples:

```text
Valid JWT

Invalid Signature

Expired JWT

Wrong Issuer

Wrong Audience

Missing Required Scope

Invalid Algorithm
```

---

## CSRF Tests

Examples:

```text
Valid CSRF Request

Missing CSRF Token

Invalid CSRF Token
```

---

## Session Tests

Examples:

```text
Authenticated Session

Logout Invalidates Session

Unauthorized Session Access
```

---

# 42. Testing Tools

The project should use Spring's testing infrastructure.

Potential areas include:

```text
Spring Boot Test

MockMvc

Spring Security Test
```

Security tests should verify actual authorization behaviour rather than only testing service logic.

---

# 43. Development Milestones

## Milestone 1 — Project Foundation

Tasks:

```text
[ ] Create Spring Boot Project

[ ] Configure Java 25

[ ] Configure Maven

[ ] Add PostgreSQL

[ ] Configure Docker Compose

[ ] Add Thymeleaf

[ ] Add Spring Security

[ ] Configure Application Profiles
```

---

## Milestone 2 — User and Authentication Foundation

Tasks:

```text
[ ] Create User Entity

[ ] Create Role Entity

[ ] Create Permission Entity

[ ] Configure Relationships

[ ] Create Repositories

[ ] Create Initial Test Users

[ ] Configure PasswordEncoder

[ ] Implement UserDetailsService
```

---

## Milestone 3 — Session-Based Security

Tasks:

```text
[ ] Configure SecurityFilterChain

[ ] Configure Public Routes

[ ] Configure Protected Routes

[ ] Create Login Page

[ ] Configure Form Login

[ ] Configure Logout

[ ] Configure Access Denied Page

[ ] Verify Session Authentication
```

---

## Milestone 4 — Authorization

Tasks:

```text
[ ] Implement Roles

[ ] Implement Permissions

[ ] Configure URL Authorization

[ ] Enable Method Security

[ ] Demonstrate @PreAuthorize

[ ] Add Thymeleaf Security Integration
```

---

## Milestone 5 — JWT Infrastructure

Tasks:

```text
[ ] Define JWT Strategy

[ ] Configure Signing Keys

[ ] Configure JwtEncoder

[ ] Configure JwtDecoder

[ ] Configure JWT Claims

[ ] Configure JWT Validation

[ ] Configure Authority Mapping
```

---

## Milestone 6 — JWT Authentication

Tasks:

```text
[ ] Create Authentication Endpoint

[ ] Authenticate User

[ ] Generate JWT

[ ] Return Access Token

[ ] Protect API With JWT

[ ] Validate JWT

[ ] Test Protected API
```

---

## Milestone 7 — Refresh Tokens

Tasks:

```text
[ ] Create RefreshToken Entity

[ ] Implement Refresh Token Storage

[ ] Implement Token Expiration

[ ] Implement Refresh Endpoint

[ ] Implement Refresh Token Rotation

[ ] Implement Token Revocation
```

---

## Milestone 8 — Advanced Security

Tasks:

```text
[ ] Review CSRF Configuration

[ ] Configure CORS

[ ] Configure Security Headers

[ ] Review Session Fixation Protection

[ ] Implement Authentication Error Handling

[ ] Implement Authorization Error Handling

[ ] Add Audit Events
```

---

## Milestone 9 — Security Dashboard

Tasks:

```text
[ ] Create Security Dashboard

[ ] Display Current Authentication

[ ] Display Roles

[ ] Display Authorities

[ ] Display Authentication Type

[ ] Demonstrate Authorization Rules
```

---

## Milestone 10 — Testing

Tasks:

```text
[ ] Authentication Tests

[ ] Authorization Tests

[ ] JWT Tests

[ ] CSRF Tests

[ ] Session Tests

[ ] Integration Tests
```

---

## Milestone 11 — Documentation

Tasks:

```text
[ ] Project Architecture

[ ] Authentication Flow

[ ] Session Authentication Flow

[ ] JWT Authentication Flow

[ ] JWT Claims Documentation

[ ] Authorization Model

[ ] Security Decisions

[ ] Threat Considerations

[ ] Local Setup Instructions

[ ] Docker Instructions

[ ] Testing Instructions
```

---

# 44. Documentation Architecture Diagrams

The README should eventually contain diagrams for:

## Session Authentication

```text
Browser
   |
   v
Login Page
   |
   v
Spring Security Authentication
   |
   v
SecurityContext
   |
   v
HTTP Session
```

## JWT Authentication

```text
Client
   |
   v
JWT
   |
   v
Authorization Header
   |
   v
JWT Validation
   |
   v
SecurityContext
   |
   v
Protected API
```

## Authorization

```text
Authenticated User
        |
        v
Authorities
        |
        v
Authorization Decision
        |
        +---- Allowed
        |
        +---- Denied
```

---

# 45. Security Best Practices

The project should consistently follow these practices.

## Never Store Passwords in Plain Text

Always use:

```text
PasswordEncoder
```

---

## Never Commit Secrets

Do not commit:

```text
Private Keys

JWT Secrets

Database Passwords

Production Credentials
```

---

## Keep JWT Access Tokens Short-Lived

Access tokens should have limited lifetime.

---

## Treat Refresh Tokens as Sensitive Credentials

Refresh tokens should support:

```text
Expiration

Revocation

Rotation
```

---

## Validate JWT Properly

JWT validation should include cryptographic signature validation and required claim validation.

---

## Do Not Put Sensitive Information in JWT Payloads

Signed JWTs are not automatically encrypted.

---

## Backend Authorization Is Mandatory

Frontend restrictions alone are not security.

Even if the UI hides a feature:

```text
The backend must still reject unauthorized access.
```

---

## Use Least Privilege

Users should only receive the permissions required for their role.

---

## Do Not Disable Security Features Without Understanding Them

Especially:

```text
CSRF

CORS

Security Headers
```

Every security decision should have a documented reason.

---

# 46. Project Learning Outcomes

After completing this project, the developer should understand:

```text
How Spring Security Works

SecurityFilterChain

Authentication

Authorization

SecurityContext

UserDetailsService

PasswordEncoder

Form Login

Session Authentication

JWT Authentication

OAuth2 Resource Server Concepts

JWT Claims

JWT Validation

Authorities

Roles

Permissions

Method Security

CSRF

CORS

Security Headers

Refresh Tokens

Token Revocation

Security Testing
```

---

# 47. Final Project Architecture

```text
                         SecurePortal
                              |
             +----------------+----------------+
             |                                 |
             v                                 v
        Thymeleaf Web                       REST API
             |                                 |
             v                                 v
     Session Authentication             JWT Authentication
             |                                 |
             +----------------+----------------+
                              |
                              v
                       Spring Security
                              |
             +----------------+----------------+
             |                                 |
             v                                 v
       Authentication                     Authorization
             |                                 |
             v                                 v
        User Details                  Roles / Permissions
             |
             v
        PasswordEncoder
             |
             v
         PostgreSQL


                    JWT Infrastructure
                           |
                +----------+----------+
                |                     |
                v                     v
            JwtEncoder           JwtDecoder
                |                     |
                v                     v
             Sign JWT           Validate JWT
```

---

# 48. Implementation Philosophy

This project should not be built as a collection of copied JWT tutorials.

The implementation philosophy will be:

> Understand the Spring Security feature being implemented, consult the official documentation, use the framework's built-in mechanism where appropriate, and write custom code only where application-specific behaviour is required.

Every major security feature should be:

```text
Implemented
      +
Tested
      +
Documented
      +
Explained
```

This will make the project both a functional application and a strong reference project for modern Spring Security.
