# ADR-0004: Compose Multiplatform shared UI

**Status:** Accepted

## Decision

Use Kotlin Multiplatform for shared data and domain code and Compose Multiplatform for the shared mobile UI. Platform source sets remain responsible for operating-system services such as biometrics, locale settings, storage paths, and application entry points.

## Consequences

- Screen behavior and navigation have one implementation across mobile targets.
- Platform integrations use small interfaces with target-specific implementations.
- A screen can still move to a native UI later because repositories and ViewModels are independent of the host shell.
