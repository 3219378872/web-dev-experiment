# Handoff: Remove Legacy hm-service Module

## Status

Implementation complete. PR #47 created, CI passing (lint/test/integration/smoke). Codex-review flagged missing harness task and stale KB content — both fixed.

## Files Changed

- `hm-service/` — deleted (66 Java files, configs, Dockerfile, test)
- `pom.xml` — removed `<module>hm-service</module>`
- `docker-compose.yml` — removed hm-service service + smoke-test dependency
- `scripts/init-nacos-routes.sh` — removed `/hi` gateway route
- `scripts/smoke/smoke.sh` — replaced `/hi` health check with `GET /items/page`
- `hm-gateway/src/main/resources/application.yaml` — removed `/hi` from auth excludePaths
- `hm-gateway/src/test/resources/application-test.yaml` — removed `/hi` from auth excludePaths
- `CLAUDE.md` — removed hm-service references
- `AGENTS.md` — removed hm-service references (byte-identical with CLAUDE.md)
- knowledge-base/modules/hm-service.md — deleted (no longer exists in tree)
- `docs/knowledge-base/INDEX.md` — removed hm-service entry
- `docs/knowledge-base/modules/hm-gateway.md` — bumped last_synced_commit
- `docs/knowledge-base/flows/auth-and-gateway-flow.md` — bumped last_synced_commit
- `docs/structure/01-system-architecture.md` — updated Mermaid diagrams and service counts
- `docs/structure/02-module-dependencies.md` — removed HM node from dependency graph
- `docs/structure/README.md` — updated service count
- `docs/agent-harness/quality-rules.md` — removed hm-service example commands

## Commands Run

- `mvn -B -ntp -q compile` — pass
- `python3 scripts/knowledge_base.py check` — pass
- `python3 scripts/agent_harness.py check` — pass
- `python3 scripts/engineering-lint.py` — pass
- `diff CLAUDE.md AGENTS.md` — pass (byte-identical)

## Known Risks

- `/hi` endpoint is gone — any external tooling depending on it will break (none found in codebase)

## Next Action

Merge PR #47 after CI passes. Move task to completed/.

## Worktree Or Branch

- `task/2026-06-03-remove-hm-service`
