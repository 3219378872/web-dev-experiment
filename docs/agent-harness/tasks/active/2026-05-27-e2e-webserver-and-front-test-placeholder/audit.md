# Audit

## Discovery

PR #15 (`align-result-r-docs`) 的 `audit.md` "Out-of-Scope Findings" 列出了
4 个 follow-up；本 PR 处理前 2 项：

1. `e2e/playwright.config.ts` 没有 `webServer` 配置 —— 本地跑 `cd e2e && npm test`
   会因 `http://localhost:5173/...` `ERR_CONNECTION_REFUSED` 失败（上次本地
   12 个 spec 全失败均为此原因）。
2. `hmall-web` / `hmall-admin` `package.json` 没有 `test` 脚本，CI smoke job
   的 `npm test --if-present` 是隐式 no-op，让"前端无单测"这个事实在 CI 中
   看不见。

## Decision

- e2e config 加 `webServer`，每个项目一个 `npm --prefix ../<frontend> run dev`，
  `reuseExistingServer: true` 兼容已经手起服的本地环境。
- 两个前端 `package.json` 各加 `test` 脚本，用 `node -e "..."` 打印一行
  "no unit tests yet (vitest setup pending)" 后 exit 0，让 CI 显式记录这个
  缺口（真正引入 vitest + dev dep + 实际 case 留给后续 PR）。
- PR1 任务记录从 `active/` 归档到 `completed/`（`scripts/agent_harness.py
  complete` 已执行）。

## Why Not Bigger

- 真正引入 vitest 会加 ~50MB devDependencies 并需要写最小有意义的测试
  样本；与本 PR "工程化补缺" 性质不符，应另起 PR。
- 修 e2e spec 失败逻辑需先把 backend stack 起起来，本环境单跑会持续
  20+min；属于 task #7（docker compose up 验证）范畴。

## Audit Table

| Requirement | Status | Evidence |
| --- | --- | --- |
| spec waived | applicable | `task.yaml.spec_waiver`：工程化补缺，无设计决策 |
| plan waived | applicable | `task.yaml.plan_waiver`：三处小改 + 归档，直接做 |
| 不破坏 backend / 前端 src / docker-compose | applicable | git diff 仅触 `e2e/playwright.config.ts` + 两个 `package.json` + tasks/ 移动 |
| `npm test` 在两个前端能跑通 | applicable | 本地 `npm test` 各打印一行 + exit 0 |
| Playwright config 仍能加载 | applicable | `npx playwright test --list` 列出 12 个 spec |
| 不引入新 tsc 错误 | applicable | `tsc --noEmit` 输出与改动前同（5 个 pre-existing 错误，0 新增） |

## Out-of-Scope Findings (carried over)

- 业务模块（cart / trade / item / notify / file / hm-api）单测覆盖近零。
- 真正引入 vitest 套件并补关键 store / util 测试。
- 修 e2e spec 失败逻辑（需要 backend stack 起着）。
- `docker compose up -d` 全栈实测 + smoke 跑通（task #7）。
- `e2e/tsconfig.json` 缺 `dom` + `node` `lib`，导致 `localStorage` /
  `__dirname` / `path` 在 tsc 下报错（不影响 Playwright 运行；后续可加
  `"lib": ["ES2022", "DOM"]` + `@types/node`）。
