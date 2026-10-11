# ADR-0006: Koin and route parameters

**Status:** Accepted

## Decision

Use Koin for dependency injection on both targets. Navigation routes carry stable IDs, and Koin supplies those IDs as ViewModel parameters. Screens do not pass serialized domain objects through navigation.

## Consequences

- Platform bootstraps load the same module list.
- Detail ViewModels fetch current data from repositories using route IDs.
- Parameterized ViewModels require explicit Koin registrations and resolution tests.
