# ADR-0001: Shared network and domain models

**Status:** Accepted

## Decision

The models in `core:model` serve as both the TMDB serialization contract and the domain model. A separate DTO layer would duplicate the same shapes and mappings without insulating the project from another data source.

## Consequences

- Callers must handle nullable fields that TMDB may omit.
- A future data source with a different contract must introduce its own DTOs and mappers.
