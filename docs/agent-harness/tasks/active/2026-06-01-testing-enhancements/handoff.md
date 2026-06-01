# Handoff: Testing Enhancements

## Status
PR 已创建。新增 AuthGlobalFilter 单元测试、Gateway 集成测试、前端 vitest、增强 smoke 测试。

## Files Changed
- `hm-gateway/src/test/java/com/hmall/gateway/filters/AuthGlobalFilterTest.java` — 8 tests
- `hm-gateway/src/test/java/com/hmall/gateway/it/GatewayAuthIntegrationTest.java` — 5 tests
- `hm-gateway/src/test/resources/application-test.yaml` — test config
- `scripts/smoke/smoke.sh` — 10→16 tests (cart+order+pay)
- `hmall-web/package.json` — vitest
- `hmall-web/vitest.config.ts` — vitest config
- `hmall-web/src/__tests__/` — 2 tests
- `hmall-admin/package.json` — vitest
- `hmall-admin/vitest.config.ts` — vitest config
- `hmall-admin/src/__tests__/` — 2 tests

## Commands Run
- `mvn -pl hm-gateway -am test -Dtest="AuthGlobalFilterTest,GatewayAuthIntegrationTest" -DfailIfNoTests=false` → 13 tests, BUILD SUCCESS
- `cmp CLAUDE.md AGENTS.md` → byte equal
- `python3 scripts/agent_harness.py check` → passed

## Known Risks
- 前端 vitest 需 npm install 后才能运行

## Next Action
等待 CI 通过后合并。

## Worktree Or Branch
- `task/2026-06-01-testing-enhancements`
