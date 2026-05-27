# Context: Align Result/R Docs to Code

## Objective
把 CLAUDE.md / AGENTS.md / quality-rules.md 中关于响应封装的描述与实际代码对齐，
消除 `Result<T>` ↔ `R<T>` 命名漂移，并把"不允许裸返 entity"修正为反映真实模式
（write 用 `R<Void>`，read 直接返回 `PageDTO<T>`/DTO，与两端 axios `r => r.data` 拦截器一致）。

## Scope
- In scope:
  - `CLAUDE.md` / `AGENTS.md`（字节镜像）—— 行 89、176 中 `Result<T>` → `R<T>`，
    并改写"不允许裸返 entity"为现行模式说明。
  - `docs/agent-harness/quality-rules.md` 第 123 行同步修正。
- Out of scope:
  - 后端 controller 接口签名重构（会破坏 hmall-web/hmall-admin 现有调用）。
  - `docs/agent-harness/tasks/completed/...` 历史任务记录（已归档不动）。

## Related Artifacts
- Spec: none - 文档与代码对齐修正，无设计决策。
- Plan: none - 单文件级别 docs-only 改动。

## Likely Files
- `CLAUDE.md`
- `AGENTS.md`
- `docs/agent-harness/quality-rules.md`

## Runtime Evidence To Inspect First
- `hm-common/src/main/java/com/hmall/common/domain/R.java`（实际类名 `R<T>`）
- `hmall-web/src/api/request.js` / `hmall-admin/src/api/request.js`（`r => r.data` 拦截器）
- 现有 controller 样本（`AddressController` 写 `R<Void>`、读 `List<Address>` 裸返）

## Safety Constraints
- 不要变更 `hm-common.R` 或 `PageDTO` 的字段、序列化形态。
- 不破坏 AGENTS↔CLAUDE 字节镜像（CI lint 校验）。

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-27-align-result-r-docs`
- Remote branch: `origin/task/2026-05-27-align-result-r-docs`
- Pull request: 推送后创建。

## Open Questions
- 无。
