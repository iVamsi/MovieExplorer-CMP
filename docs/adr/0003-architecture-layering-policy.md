# ADR-0003: Architecture layering policy

**Status:** Accepted

## Decision

- Feature code may depend on domain repository interfaces and use cases.
- Feature code must not import data implementations.
- Repository interfaces live in `core:domain`.
- Shared source must not import JVM or platform APIs.
- Use cases represent shared mutations; read-only detail flows may use repositories directly.

Konsist tests enforce these boundaries.
