# Context: Frontend Design Finalize

## Objective
提交 `feat/fronted-design` 分支上残留的未提交前端视觉对齐改动（hmall-web/hmall-admin 多页 + e2e 视觉回归调整），清理误入文件，修复一处分页功能回归，过 CI 后合并。

## Scope
- In scope:
  - hmall-web / hmall-admin 多页面的高保真原型视觉对齐收尾（样式/结构，保持 API 与数据逻辑不变）。
  - hmall-web 新增 `assets/styles/components.css` 在 `main.js` 的引入。
  - e2e 视觉回归用例与 fixtures/utils 调整。
  - 修复 `hmall-admin/src/views/OrderList.vue` 分页功能回归（装饰性死分页 → 复用 `fetch(p)` 的功能性分页）。
  - 修复 `hmall-web` 单测 `App.spec.ts`（缺少 router 上下文导致 `useRoute()` 崩溃）。
  - 清理误入仓库的调试产物与忽略规则。
  - harness 任务记录 + KB 页 co-change（K005）。
  - 修正 `docs/backend-api.md`：将前端 api/*.js 调用逐条比对 `docs/api.md` 与后端 Controller 源码，纠正旧版误报（已实现却列为缺失）并补漏报（前端 404 的路径不一致）。
- Out of scope:
  - 任何后端 / API 契约 / 数据库改动。
  - 既有 endpoint 返回形态变更。
  - 修改 `CLAUDE.md` / `AGENTS.md`。
  - **admin Dashboard 运营数据接入**：成交额/订单/新增用户/访客/销售趋势/品类占比/
    热销/待办/最新订单所需的后端统计接口当前不存在（见 `docs/backend-api.md` B4），
    本页仅做视觉对齐 + 演示数据，真实数据待后端统计接口接入后再替换。main 版本
    同样无任何后端数据调用，故不构成"破坏既有 API/数据逻辑"。

## Related Artifacts
- Spec: none —— 纯前端视觉收尾，无 API/数据/行为破坏性变更（见 task.yaml spec_waiver）。
- Plan: none —— 收尾性任务，范围明确（见 task.yaml plan_waiver）。

## Likely Files
- `hmall-web/src/{App.vue,main.js,components/*,views/*,styles/global.css}`
- `hmall-web/src/__tests__/App.spec.ts`
- `hmall-admin/src/{components/AdminLayout.vue,views/*}`
- `e2e/visual/{fixtures.ts,regression.spec.ts,utils.ts}`
- `.gitignore`
- `docs/knowledge-base/modules/{hmall-web,hmall-admin}.md`

## Runtime Evidence To Inspect First
- 已提交对齐页的分页范式：`hmall-admin` 各列表页 HEAD 版用 `el-pagination @current-change="fetch"`；`hmall-web/src/views/OrderList.vue` 用功能性自定义分页（`totalPages`/`pageRange`/prev/next + `@click="fetchOrders(p)"`）。
- `hmall-admin/src/views/OrderList.vue` `fetch(p=1)` 真正按页查询 `getOrders({page,size})`。
- `hmall-admin/src/views/FeedbackList.vue` `fetch()` 调 `getFeedbacks()` 不传分页参数 —— 数据层本就不分页，分页器装饰化不构成行为回归（保留现状）。

## Safety Constraints
- 不提交任何密钥。
- 不破坏 `hm-common` 的 `R<T>`/`PageDTO` 响应封装（本任务不碰后端代码）。
- 不无前端协调地改既有 endpoint 形态。
- 误入文件（空文件 EOF、e2e/visual 下 *.js 一次性调试脚本与 test-mock 调试 spec、根 test-results/）不得进入提交。

## Worktree Or Branch
- `task/2026-06-03-frontend-design-finalize`（由原 `feat/fronted-design` 按 harness 规范重命名，历史保留）

## Open Questions
- none
