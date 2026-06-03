# Context: Remove Legacy hm-service Module

## Objective

Remove the legacy `hm-service` monolithic module that was the original all-in-one backend, now fully superseded by individual microservices.

## Scope

- In scope: delete hm-service directory, remove all references from build/deploy/config/docs, replace `/hi` health check
- Out of scope: database tables (shared with microservices), historical task records, hm-common module

## Related Artifacts

- Spec: none — straightforward removal of dead code, no design decisions needed
- Plan: inline in PR description

## Likely Files

- `hm-service/` (entire directory — delete)
- `pom.xml` (module declaration)
- `docker-compose.yml` (service definition)
- `scripts/init-nacos-routes.sh` (gateway route)
- `scripts/smoke/smoke.sh` (health check endpoint)
- `hm-gateway/src/main/resources/application.yaml` (auth exclude)
- `hm-gateway/src/test/resources/application-test.yaml` (auth exclude)
- `CLAUDE.md`, `AGENTS.md` (module descriptions)
- knowledge-base/modules/hm-service.md (deleted — no longer exists in tree)
- `docs/knowledge-base/INDEX.md` (remove entry)
- `docs/structure/01-system-architecture.md` (Mermaid diagrams)
- `docs/structure/02-module-dependencies.md` (dependency graph)
- `docs/structure/README.md` (service count)
- `docs/agent-harness/quality-rules.md` (example commands)

## Runtime Evidence To Inspect First

- `mvn compile` passes without hm-service module
- `docker compose config -q` validates without hm-service service

## Safety Constraints

- Do not commit secrets.
- Do not break public API response envelopes.
- CLAUDE.md and AGENTS.md must remain byte-identical.

## Worktree Or Branch

- `task/2026-06-03-remove-hm-service`

## Open Questions

- none
