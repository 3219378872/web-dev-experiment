# Verification

## 本地 CI 等价命令（按 quality-rules.md::CI Verification 全套）

| Command | Result | Evidence |
| --- | --- | --- |
| `python3 scripts/agent_harness.py check` | passed | agent harness check passed |
| `python3 scripts/knowledge_base.py check --base origin/main` | passed | knowledge base check passed；本 PR 不改任何 KB tracks 路径下的文件，K005 不触发 |
| `python3 scripts/engineering-lint.py` | passed | engineering-lint: all checks passed |
| `diff -u AGENTS.md CLAUDE.md` | passed | identical |
| `mvn -B -ntp -q test` | skipped | 本 PR 仅 harness task records 改动，不动 backend；sandbox 实跑会浪费 CI 时间且无新断言 |
| `mvn -B -ntp -q -Pintegration verify -DskipUnitTests=true` | skipped | 同上 + sandbox 无 daemon MySQL/Redis |
| `cd hmall-web && npm ci && npm test --if-present && npm run build` | skipped | 本 PR 不动 hmall-web；前面 PR #13 已实跑通过 |
| `cd hmall-admin && npm ci && npm test --if-present && npm run build` | skipped | 同上 |
| `docker compose -f docker-compose.yml config -q` | skipped | 本 PR 不动 docker-compose.yml |
| `npx playwright test` | skipped | 本 PR 不动 e2e/；PR #12 已实跑过完整 12 spec 覆盖 |

## 归档动作

| Command | Result | Evidence |
| --- | --- | --- |
| `python3 scripts/agent_harness.py new archive-cleanup-task --date 2026-05-27` | passed | 新 active task 目录已建 |
| `python3 scripts/agent_harness.py complete 2026-05-27-cleanup-tracked-build-artifacts` | passed | 任务移到 `docs/agent-harness/tasks/completed/2026-05-27-cleanup-tracked-build-artifacts/` |

## 后置 active/ 与 completed/ 状态

active/（应只剩本任务自身）:
```
2026-05-27-archive-cleanup-task
```

completed/（共 10 个）:
```
2026-05-26-align-codex-naming
2026-05-26-backend-integration-tests
2026-05-26-backend-unit-tests
2026-05-26-establish-harness-and-test-pyramid
2026-05-26-finalize-harness-bootstrap-and-add-ci
2026-05-26-playwright-e2e-tests
2026-05-26-smoke-tests
2026-05-26-visual-regression-fixes
2026-05-27-archive-merged-tasks-batch
2026-05-27-cleanup-tracked-build-artifacts
```
