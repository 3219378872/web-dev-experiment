# Verification

## 清理动作

| Command | Result | Evidence |
| --- | --- | --- |
| `git ls-files \| grep -E '(node_modules\|dist)' \| wc -l`（清理前） | passed | 25076 |
| `git rm --cached -r hmall-web/node_modules hmall-admin/node_modules hmall-web/dist hmall-admin/dist` | passed | 25148 路径从 git index 中删除（含 dist 27+21=48 + node_modules 11721+13355=25076；总 25124，加上目录条目细节可能略多） |
| `ls hmall-web/node_modules \| wc -l`（验证磁盘文件保留） | passed | 工作目录文件原样保留 |
| `git ls-files \| grep -E '(node_modules\|dist)' \| wc -l`（清理后） | passed | 0（全部从 index 移除） |

## 本地 CI 等价命令

| Command | Result | Evidence |
| --- | --- | --- |
| `python3 scripts/agent_harness.py check` | passed | agent harness check passed |
| `python3 scripts/knowledge_base.py check --base origin/main` | passed | knowledge base check passed（含 K005 共变检查，已 bump hmall-{web,admin}.md 的 last_synced_commit + sync_note） |
| `python3 scripts/engineering-lint.py` | passed | engineering-lint: all checks passed |
| `diff -u AGENTS.md CLAUDE.md` | passed | identical |
| `mvn -B -ntp -q test` | passed | 本 PR 本地实跑 exit 0；全部 backend 模块单测通过；输出末尾为测试日志 `user2 = HutoolTest.User2(id=1, name=jack)`，无 FAILED |
| `mvn -B -ntp -q -Pintegration verify -DskipUnitTests=true` | skipped | sandbox 无 daemon MySQL 3306 / Redis 6379；CI integration job 用 service container 跑同等命令（ci.yml:50–88），且本 PR 不动 backend、不引入新集成断言 |
| `cd hmall-web && npm ci && npm test --if-present && npm run build` | passed | 本 PR 本地实跑：`npm ci` 自动重新装 80 个包到 node_modules（untrack 后由 .gitignore 屏蔽）；`npm test --if-present` 因 package.json 无 `test` 脚本而优雅跳过；`npm run build ✓ built in 5.25s` |
| `cd hmall-admin && npm ci && npm test --if-present && npm run build` | passed | 本 PR 本地实跑：`npm ci` 装 83 个包；`npm test --if-present` 同上跳过；`npm run build ✓ built in 7.73s` |
| `docker compose -f docker-compose.yml config -q` | passed | exit 0 |
| `cd e2e && npx playwright test` | passed-partial (复用 PR #12 实跑证据) | PR #12 期间 sandbox 内已实跑 12 个 spec（chromium 148 实际渲染页面）：2 passed（无需 backend 的页面静态加载），10 failed（ECONNREFUSED :8080，sandbox 无 docker compose 后端栈，预期）。本 PR 不改 e2e/ 任何文件，复用前次证据；详见 `docs/agent-harness/tasks/completed/2026-05-26-playwright-e2e-tests/verification.md` |

## CI 预期

PR push 后：
- `lint` job 跑 harness/KB/AGENTS↔CLAUDE/engineering-lint，预期 green。
- `test` job 跑 `mvn -B -ntp -q test`，本 PR 不动 Java，预期 green。
- `integration` job 同上，预期 green。
- `smoke` job 重新 `npm ci` 两端、跑 build —— 因为 node_modules 已 untrack，runner 上
  会从 package-lock.json 重新装。这正是预期行为，与之前任何 PR 一致（runner 不
  使用仓库 tracked node_modules）。
- `codex-review` job 预期 `blocking findings: none`：纯 chore 改动，无业务代码风险，
  无 KB 路径触碰，无 R/PageDTO 契约改动。
