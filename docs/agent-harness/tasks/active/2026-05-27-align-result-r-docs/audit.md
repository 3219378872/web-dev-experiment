# Audit

## Discovery

`/goal` 审查阶段对照 `hm-common/src/main/java/com/hmall/common/domain/R.java`
与所有 controller / 前端 axios 拦截器发现以下漂移：

1. **类名漂移**：`CLAUDE.md` / `AGENTS.md` / `docs/agent-harness/quality-rules.md`
   多处引用 `Result<T>`，实际类名为 `R<T>`（包 `com.hmall.common.domain`）。
2. **规则与实现不一致**：旧文档断言 "API 必须用 `Result<T>` 封装，不允许裸返
   entity"，但代码实际是**两组并存**的混合状态：

   **新版接口（多数返 `R<Void>`/`*VO`）**：
   - `item-service/*` 所有 controller（`ItemController`/`CategoryController`/
     `ReviewController`/`SearchController`）
   - `notify-service/*` 所有 controller（`NotificationController` /
     `CustomerMessageController` / `FeedbackController`）
   - `user-service/AddressController`（写）+ `FavoriteController` + `UserController`
     的 `sendCode` / `register` / `resetPassword` / `updateProfile`
   - `trade-service/OrderController` 的 `cancel` / `confirm` / `refund` /
     `updateStatus` / `ship`、`trade-service/CouponController` 的写操作

   **遗留接口（多数返 `void` / 裸值 / 裸 entity）**：
   - `hm-service/*` 聚合服务（写多数 `void`、读多数裸 `*VO`/`*DTO`）
   - `pay-service/PayController`（`applyPayOrder` → `String`、
     `tryPayOrderByBalance` → `void`）
   - `cart-service/CartController`（所有写返 `void`、`queryMyCarts` 返
     `List<CartVO>`）
   - `trade-service/OrderController.{createOrder→Long, markOrderPaySuccess→void,
     updateOrder→void}`、`queryOrderById→OrderVO`
   - `user-service/UserController.{login→UserLoginVO, deductMoney→void}`、
     `AddressController.list() → List<Address>`（**裸返数据库 entity**）

   读路径几乎全是裸返 `PageDTO<T>` / `*VO` / `*DTO` / `List<*>`，前端 axios
   拦截器 `r => r.data` 兼容两种形态。

## Decision

不改 controller 签名（会破坏前端），改为更新文档**如实描述当前混合状态**并
为未来新增 endpoint 指定优先方向：

- 类名全部统一 `R<T>` / `R<Void>`，包 `com.hmall.common.domain`。
- 异常路径"由 `CommonExceptionAdvice` 统一包成 `R<Void>`" 作为唯一全栈一致约定。
- 成功路径承认混合，列出新版/遗留两组归属，前端 axios 双兼容。
- 新增 endpoint **指引**：`R<Void>`(写) + `*VO`/`PageDTO<T>`(读)；不要无前端
  协调地改既有 endpoint 形态。

## Codex-review Round 1 Findings & Response

| Finding | 处理 |
| --- | --- |
| 1) 新文本仍说 "write 返 R<Void>" 与代码不符（`createOrder→Long`、`login→UserLoginVO`、`markOrderPaySuccess→void`、`addItem2Cart→void`、`AddressController.list()→List<Address>` 等） | 把规则改为如实描述异常一致/成功混合的当前状态，列出新版/遗留两组归属 |
| 2) CLAUDE.md/AGENTS.md:104 `hm-common.dto.Result` 漏改 | 改为 `hm-common.domain.R` + 同时声明 `PageDTO` |
| 3) `docs/knowledge-base/modules/{hm-common,hmall-web}.md` 仍说 "全部 controller 返 R<T>/PageDTO<T>" 与新约定矛盾 | 两文件同步描述与 last_synced_commit（883f5fb） |
| 4) verification.md 缺前端/build/docker/playwright 证据；handoff.md "待推/待开" 与 task.yaml `pr-open` 不一致 | verification.md 加全量 evidence 含 N/A reason；handoff.md 重写至 pr-open + 第二轮 ready 状态 |

## Audit Table

| Requirement | Status | Evidence |
| --- | --- | --- |
| spec waived | applicable | `task.yaml.spec_waiver` |
| plan waived | applicable | `task.yaml.plan_waiver` |
| 不破坏 R / PageDTO 行为 | applicable | 仅改 markdown，无 Java 源码改动 |
| AGENTS↔CLAUDE 字节镜像 | applicable | `cmp CLAUDE.md AGENTS.md` byte equal |
| KB last_synced 同步 | applicable | hm-common.md / hmall-web.md 提升至 883f5fb |
| CI lint/test/integration/smoke | pass(round 1) | 详见 PR #15 第一轮 CI |
| codex-review | pending(round 2) | 第一轮 4 blocking findings 已处理；第二轮等审 |

## Out-of-Scope Findings (for follow-up)

- 业务模块（cart / trade / item / notify / file / hm-api）单测覆盖几乎为零，
  与 CLAUDE.md "目标核心 service/util 80% 覆盖率" 差距明显。
- `e2e/playwright.config.ts` 无 `webServer` 配置，跑前需手动启动两个前端，
  上次本地运行（`e2e/test-results` 残留，gitignored，CI 无）12 个 spec
  全失败（`ERR_CONNECTION_REFUSED`）。
- `hmall-web` / `hmall-admin` `package.json` 没有 `test` 脚本（前端 0 个 vitest）。
- 响应封装真正统一为一组（要么 `R<T>` 全包、要么明确"读裸返+写 R<Void>"）需配套
  前端 axios 调用迁移；是 breaking change，应另起 spec + plan。

以上将在后续 PR 中分别处理。
