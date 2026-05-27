# Audit

## Discovery

`/goal` 审查阶段对照 `hm-common/.../R.java` 与现有 controller / 前端 axios 拦截器
发现以下漂移：

1. **类名漂移**：`CLAUDE.md` / `AGENTS.md` / `docs/agent-harness/quality-rules.md`
   多处引用 `Result<T>`，实际类名为 `R<T>`（包 `com.hmall.common.domain`）。
2. **规则与实现不一致**：`CLAUDE.md:176` 写 "API 必须用 `Result<T>` 封装，不允许
   裸返 entity"，但仓库实际模式是：
   - 写操作（POST/PUT/DELETE）：返回 `R<Void>`，异常经 `CommonExceptionAdvice`
     统一包装为 `R.error(...)`（仅这一处会自动生成 `R` 错误体）。
   - 只读接口：直接返回 `PageDTO<T>` / `*VO` / `*DTO`（如
     `AddressController.list() → List<Address>`、
     `ItemController.queryItemById() → ItemDTO`、
     `OrderController.queryOrderById() → OrderVO`）。
   - 两个前端（`hmall-web` / `hmall-admin`）axios 拦截器统一 `r => r.data`，
     调用点按 raw 形态消费（如 `data?.list || []`）—— 任何改成 `R<T>` 包裹的
     尝试都会破坏现有前端。

## Decision

不重构 controller 签名，改为更新文档反映真实模式：
- 命名统一 `R<T>`。
- 把"裸返 entity"红线限定到"**数据库** entity"（保留 DTO / VO / PageDTO 的合法
  裸返作为读路径模式）。

## Audit Table

| Requirement | Status | Evidence |
| --- | --- | --- |
| spec waived | applicable | `task.yaml.spec_waiver`：docs-only 对齐修正，无设计决策 |
| plan waived | applicable | `task.yaml.plan_waiver`：单 commit docs 改动，无多步实施 |
| 不破坏 R / PageDTO 行为 | applicable | 仅改 markdown，无 Java 源码改动 |
| AGENTS↔CLAUDE 字节镜像 | applicable | `cmp CLAUDE.md AGENTS.md` byte equal |

## Out-of-Scope Findings (for follow-up)

- 业务模块（cart / trade / item / notify / file / hm-api）单测覆盖几乎为零，
  与 CLAUDE.md "目标核心 service/util 80% 覆盖率" 差距明显。
- `e2e/playwright.config.ts` 无 `webServer` 配置，跑前需手动启动两个前端，
  上次 `e2e/test-results/.last-run.json` 12 个 spec 全失败
  （`ERR_CONNECTION_REFUSED`）。
- `hmall-web` / `hmall-admin` `package.json` 没有 `test` 脚本（前端 0 个 vitest）。

以上将在后续 PR 中分别处理。
