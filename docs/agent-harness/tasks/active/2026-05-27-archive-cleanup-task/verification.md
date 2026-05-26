# Verification

## 本地 CI 等价命令（按 quality-rules.md::CI Verification 全套实跑）

| Command | Result | Evidence |
| --- | --- | --- |
| `python3 scripts/agent_harness.py check` | passed | agent harness check passed |
| `python3 scripts/knowledge_base.py check --base origin/main` | passed | knowledge base check passed；本 PR 不改任何 KB tracks 路径下的文件，K005 不触发 |
| `python3 scripts/engineering-lint.py` | passed | engineering-lint: all checks passed |
| `diff -u AGENTS.md CLAUDE.md` | passed | identical |
| `mvn -B -ntp -q test` | passed | 本 PR 本地实跑 exit 0；全部 backend 模块单测通过；输出末尾为正常测试日志 `user2 = HutoolTest.User2(id=1, name=jack)`，无 FAILED |
| `mvn -B -ntp -q -Pintegration verify -DskipUnitTests=true` | skipped | sandbox 无 daemon MySQL 3306 / Redis 6379；CI integration job 用 service container 跑同等命令（ci.yml:50–88），本 PR 不动 backend、不引入新集成断言 |
| `cd hmall-web && npm ci && npm test --if-present && npm run build` | passed | 本 PR 本地实跑：`npm ci` 装包；`npm test --if-present` 因 package.json 无 `test` 脚本而优雅跳过；`npm run build ✓ built in 4.60s` |
| `cd hmall-admin && npm ci && npm test --if-present && npm run build` | passed | 本 PR 本地实跑：`npm ci` 装包；`npm test --if-present` 同上跳过；`npm run build ✓ built in 6.58s` |
| `docker compose -f docker-compose.yml config -q` | passed | 本 PR 本地实跑 exit 0 |
| `npx playwright test` | skipped | 本 PR 不动 e2e/；PR #12 已在 sandbox 完整实跑 chromium + 12 spec（2 passed + 10 ECONNREFUSED 预期），结果归档在 `docs/agent-harness/tasks/completed/2026-05-26-playwright-e2e-tests/verification.md` |

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
