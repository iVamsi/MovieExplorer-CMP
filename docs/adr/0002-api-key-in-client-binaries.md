# ADR-0002: TMDB API key in client binaries

**Status:** Accepted risk

## Decision

The build generates the TMDB key into `AppConfig` for both client binaries. The key is recoverable from a distributed binary, so it must remain limited to TMDB access and must never protect private infrastructure or secrets.

## Mitigations

- Source the key from `local.properties`, an environment variable, or the CI secret.
- Keep the key out of source control and rotate it if TMDB reports abuse.
- Revisit this decision if the key gains billing or broader privileges.
