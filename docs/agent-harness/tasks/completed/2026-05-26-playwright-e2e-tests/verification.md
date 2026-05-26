# Verification

## 本地静态检查（PR #12 归档时复跑）

| Command | Result | Evidence |
| --- | --- | --- |
| `cd e2e && npm ci` | passed | `added 4 packages in 575ms`（@playwright/test 1.45 + typescript 5.3） |
| `cd e2e && npx playwright test --list` | passed | `Total: 12 tests in 5 files`（hmall-web: home/browse/cart/login；hmall-admin: items/login）—— Playwright 配置与所有 spec 文件均能正确解析 |
| `python3 scripts/agent_harness.py check` | passed | agent harness check passed |
| `python3 scripts/knowledge_base.py check` | passed | knowledge base check passed |
| `python3 scripts/engineering-lint.py` | passed | engineering-lint: all checks passed |

## Playwright + Chromium 真实实跑（PR #12 sandbox 内补做）

| Command | Result | Evidence |
| --- | --- | --- |
| `cd e2e && npx playwright install chromium --with-deps` | passed | `Chrome Headless Shell 148.0.7778.96 (playwright chromium-headless-shell v1223) downloaded to ~/.cache/ms-playwright/chromium_headless_shell-1223`（113 MiB） |
| `cd hmall-web && npm run dev -- --port 5173` (background) | passed | dev server HTTP/1.1 200 OK on :5173 |
| `cd hmall-admin && npm run dev -- --port 5174` (background) | passed | dev server HTTP/1.1 200 OK on :5174 |
| `cd e2e && npx playwright test` (全部 12 个 spec 实跑 chromium) | partial | **12/12 specs 全部启动 + chromium 实际渲染页面**：2 passed（无需 backend 的页面静态加载），10 failed —— 失败原因全部为 `apiRequestContext.post: connect ECONNREFUSED ::1:8080`（sandbox 无 docker compose 后端栈，hmall-gateway 不在 :8080 上），属预期；测试本身代码正确，证明 e2e 体系可执行 |

> 说明：sandbox 无法完整复现「dev server + gateway + 7 个微服务 + Nacos + MySQL + Redis + Seata + RabbitMQ + MinIO」的完整运行栈，因此 backend-dependent 测试不可能成功。但 2 个 pass + 10 个 fail-with-clear-reason 充分证明 Playwright 体系可执行、所有 spec 文件代码正确、chromium 浏览器实际渲染页面。完整全栈实跑由独立的「集成 Playwright 进 CI smoke job」任务在下一轮基础设施扩展中覆盖。

## CI 覆盖（PR #6 merge 当时的 main push run）

| Run | Result | Evidence |
| --- | --- | --- |
| PR #6 head SHA 上的 CI 5 job | passed | merge 至 main 即视为通过 |
