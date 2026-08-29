# Releasing

The container release workflow publishes SecurePortal to GitHub Container Registry (GHCR) when a signed version tag is pushed. It uses the repository-scoped `GITHUB_TOKEN`; no registry password is stored in repository secrets.

## Create a release image

After the CI workflow is green, create and push a version tag:

```shell
git tag -s v0.1.0 -m "SecurePortal 0.1.0"
git push origin v0.1.0
```

The workflow publishes these image tags:

- `ghcr.io/bansikah22/spring-security-jwt:0.1.0`
- `ghcr.io/bansikah22/spring-security-jwt:0.1`
- `ghcr.io/bansikah22/spring-security-jwt:sha-<commit>`

Each image receives a software bill of materials (SBOM) and SLSA build provenance attestation from GitHub Actions. Keep the immutable version or SHA tag in Kubernetes manifests; do not deploy a floating tag.

The package is private by default. Change its visibility in GitHub Packages only when public image pull access is intended.