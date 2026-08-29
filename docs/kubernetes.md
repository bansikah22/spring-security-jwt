# Kubernetes

The [Kubernetes manifests](https://github.com/bansikah22/spring-security-jwt/tree/master/kubernetes) deploy SecurePortal with runtime hardening and health probes.

## Database options

- Use an existing managed PostgreSQL service: set `DATABASE_URL`, `DATABASE_USERNAME`, and provision `DATABASE_PASSWORD` through your secret manager.
- Use the optional pinned PostgreSQL StatefulSet for local, development, or small non-critical environments.

The application pods run as a non-root user, drop capabilities, use `RuntimeDefault` seccomp, disable service-account token mounting, and use a read-only filesystem with an empty `/tmp` volume.

See the repository's [Kubernetes guide](https://github.com/bansikah22/spring-security-jwt/blob/master/kubernetes/README.md) before applying a manifest.