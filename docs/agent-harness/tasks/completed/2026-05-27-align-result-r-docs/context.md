# Context: Align Result/R Docs to Code

## Objective

把 `CLAUDE.md` / `AGENTS.md` / `docs/agent-harness/quality-rules.md` 与
`docs/knowledge-base/modules/{hm-common,hmall-web}.md` 中关于响应封装的描述与
实际代码对齐：

- 消除 `Result<T>` ↔ `R<T>` 命名漂移（实际类是 `com.hmall.common.domain.R`）。
- 用如实描述替代之前的"必须用 `R<T>` 封装、不允许裸返 entity"伪规则。
  实际代码是**混合状态**：异常路径由 `CommonExceptionAdvice` 统一包成
  `R<Void>`（一致）；成功路径分新版/遗留两组并存，包含 `R<Void>`、`R<T>`、
  `*VO`/`*DTO`/`PageDTO<T>`、裸 `void`/`Long`/`String`，甚至裸返数据库 entity
  （如 `user-service/AddressController.list()` → `List<Address>`、
  `trade-service/OrderController.createOrder()` → `Long`、
  `cart-service/CartController.addItem2Cart()` → `void`、
  `pay-service/PayController.applyPayOrder()` → `String`）。两个前端 axios
  `r => r.data` 拦截器对两种形态都兼容。

## Scope

- In scope:
  - `CLAUDE.md` / `AGENTS.md`（字节镜像）"Module Map" `hm-common` 段、文末
    类名段（旧 `hm-common.dto.Result` → `hm-common.domain.R`）、"Safety" 段。
  - `docs/agent-harness/quality-rules.md` "API Contract Safety" 段。
  - `docs/knowledge-base/modules/hm-common.md` "公开接口与契约"/"注意事项"段
    + frontmatter `last_synced_commit` / `sync_note`。
  - `docs/knowledge-base/modules/hmall-web.md` "公开接口与契约"段 + frontmatter。
  - 本任务 harness 记录（context / verification / audit / handoff / task.yaml）。
- Out of scope:
  - 后端 controller 接口签名重构（会破坏 hmall-web/hmall-admin 现有调用）。
  - `docs/agent-harness/tasks/completed/...` 历史任务记录（归档不动）。
  - 前端 `npm test` 占位、e2e `webServer` 自动化、业务模块单测补齐 ——
    单独 PR 处理（见 `audit.md` "Out-of-Scope Findings"）。

## Related Artifacts

- Spec: none - 文档与代码对齐修正，无设计决策。
- Plan: none - 单 PR 文档改动，无多步实施。

## Likely Files

- `CLAUDE.md`
- `AGENTS.md`
- `docs/agent-harness/quality-rules.md`
- `docs/knowledge-base/modules/hm-common.md`
- `docs/knowledge-base/modules/hmall-web.md`

## Runtime Evidence To Inspect First

- `hm-common/src/main/java/com/hmall/common/domain/R.java`（实际类名 `R<T>`）
- `hmall-web/src/api/request.js` / `hmall-admin/src/api/request.js`
  （`r => r.data` 拦截器）
- 全量 controller 写/读返类型扫描（详见 `audit.md` "Discovery"）

## Safety Constraints

- 不要变更 `hm-common.domain.R` 或 `PageDTO` 的字段、序列化形态。
- 不破坏 AGENTS↔CLAUDE 字节镜像（CI lint 校验）。
- 不改任何 Java / Vue / SQL / docker-compose 源码。

## Branch And PR

- Base branch: `main`
- Task branch: `task/2026-05-27-align-result-r-docs`
- Remote branch: `origin/task/2026-05-27-align-result-r-docs`
- Pull request: https://github.com/3219378872/web-dev-experiment/pull/15

## Open Questions

- 无。
