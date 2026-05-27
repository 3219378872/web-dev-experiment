# Verification

## Local Checks（current branch `task/2026-05-27-e2e-webserver-and-front-test-placeholder`）

| Command | Result | Evidence / Reason |
| --- | --- | --- |
| `cmp CLAUDE.md AGENTS.md` | pass | byte equal（本 PR 不改 CLAUDE.md/AGENTS.md，但 lint 仍校验） |
| `python3 scripts/agent_harness.py check` | pass | `agent harness check passed` |
| `python3 scripts/knowledge_base.py check --base origin/main` | pass | `knowledge base check passed`（本 PR 不动 KB tracks 路径） |
| `python3 scripts/engineering-lint.py` | pass | `engineering-lint: all checks passed` |
| `mvn -B -ntp test` | pass | 当前分支 HEAD `BUILD SUCCESS`（80 个测试 0 失败；后端无源码改动） |
| `cd hmall-web && npm test` | pass | 输出 `hmall-web has no unit tests yet (vitest setup pending)`，exit 0 |
| `cd hmall-admin && npm test` | pass | 输出 `hmall-admin has no unit tests yet (vitest setup pending)`，exit 0 |
| `cd hmall-web && npm run build` | pass | `✓ built in 5.48s`（无回归） |
| `cd hmall-admin && npm run build` | pass | `✓ built in 8.58s`（无回归） |
| `cd e2e && npx playwright test --list` | pass | 列出 12 个 spec，无 config 加载错误 |
| `npx --prefix e2e tsc --noEmit --project e2e/tsconfig.json` | pre-existing errors | 5 个 pre-existing 错误（`localStorage` ×3、`path`、`__dirname`），**0 新增**；本 PR 改动不引入新 tsc 错误 |
| `docker compose config -q` | pass | exit 0（compose 文件不变） |
| Playwright 实跑 e2e | not run | 需 backend gateway + 前端 + 用户数据齐套；e2e webServer 仅自动起前端不起 backend；视为 follow-up（task #7） |

## Evidence Table（PR 改动）

| 文件 | 修正 |
| --- | --- |
| `e2e/playwright.config.ts` | 加 `webServer` 数组（hmall-web:5173 + hmall-admin:5174），`reuseExistingServer: true`，备注后端 gateway 仍需独立起 |
| `hmall-web/package.json` scripts | 加 `"test": "node -e \"console.log('...')\""` 显式 placeholder |
| `hmall-admin/package.json` scripts | 同上 |
| `docs/agent-harness/tasks/{active → completed}/2026-05-27-align-result-r-docs/*` | PR #15 任务归档 |
| `docs/agent-harness/tasks/active/2026-05-27-e2e-webserver-and-front-test-placeholder/` | 本任务 harness 记录 |

## What This Does NOT Change

- 任何 backend Java / SQL / docker-compose
- 任何 vite/vue 源码、路由、组件
- 前端 axios 拦截器或调用点
- `hm-common.R` / `PageDTO` 等公共契约类型
- e2e spec 业务逻辑（仅 config 改）

## How to Reverify

```bash
git fetch origin task/2026-05-27-e2e-webserver-and-front-test-placeholder
git checkout task/2026-05-27-e2e-webserver-and-front-test-placeholder
cmp CLAUDE.md AGENTS.md
python3 scripts/agent_harness.py check
python3 scripts/knowledge_base.py check --base origin/main
python3 scripts/engineering-lint.py
mvn -B -ntp test
(cd hmall-web && npm test && npm run build)
(cd hmall-admin && npm test && npm run build)
(cd e2e && npx playwright test --list)
docker compose config -q
```
