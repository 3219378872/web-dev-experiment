# Context: e2e webServer + frontend test placeholder + 归档 PR1

## Objective

三件小改动打包成一个 PR：

1. **e2e Playwright `webServer` 自动启动**：`e2e/playwright.config.ts` 加 `webServer`
   配置，本地跑 `cd e2e && npm test` 时自动启动 `hmall-web` (5173) 与
   `hmall-admin` (5174) 两个 Vite dev 服务器；用 `reuseExistingServer: true`
   保证已手动起服的环境不会冲突。后端 gateway (8080) 仍需独立起。
2. **前端 `npm test` 占位脚本**：`hmall-web` / `hmall-admin` 的 `package.json`
   各加一个 `test` 脚本（`node -e "console.log(...)"` 形态），让 CI smoke job 的
   `npm test --if-present` 不再是隐式 no-op，明确暴露"还没装 vitest"这个缺口。
3. **归档 PR1 任务记录**：`docs/agent-harness/tasks/active/2026-05-27-align-result-r-docs/`
   → `completed/`（由 `scripts/agent_harness.py complete` 触发，PR #15 合并后做）。

## Scope

- In scope:
  - `e2e/playwright.config.ts`
  - `hmall-web/package.json`
  - `hmall-admin/package.json`
  - `docs/agent-harness/tasks/{active → completed}/2026-05-27-align-result-r-docs/`
  - 本任务 harness 记录
- Out of scope:
  - 修 e2e spec 失败逻辑（缺 token、selector 不匹配等）—— 需先把 backend stack
    起起来跑一遍再针对性修。
  - 真正引入 vitest + 写最小测试 —— 加重 dep，单独 PR。
  - playwright tsconfig 已有的 `localStorage` / `path` / `__dirname` 类型缺失 ——
    与本 PR 范围无关，pre-existing。

## Related Artifacts

- Spec: none - 工程化改动，无设计决策。
- Plan: none - 三处小改 + 归档，直接做即可。

## Likely Files

- `e2e/playwright.config.ts`
- `hmall-web/package.json`
- `hmall-admin/package.json`
- `docs/agent-harness/tasks/active/2026-05-27-align-result-r-docs/`（归档）

## Runtime Evidence To Inspect First

- 前端各自 `package.json scripts` 当前形态（无 `test`）
- `e2e/playwright.config.ts` 当前缺 `webServer`
- 上次本地 e2e 跑：12 个 spec 全失败，原因均为 `ERR_CONNECTION_REFUSED`

## Safety Constraints

- 不动 backend Java / SQL / docker-compose 任何文件。
- 不动 vite/vue 源码或路由。
- `webServer` 用 `reuseExistingServer: true`，不抢占已有 dev server。

## Branch And PR

- Base branch: `main`
- Task branch: `task/2026-05-27-e2e-webserver-and-front-test-placeholder`
- Remote branch: `origin/task/2026-05-27-e2e-webserver-and-front-test-placeholder`
- Pull request: 推送后创建。

## Open Questions

- 无。
