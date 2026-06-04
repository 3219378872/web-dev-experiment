# Backend API Fulfillment — 设计 spec

- **日期**：2026-06-04
- **来源清单**：`docs/backend-api.md`（前端对齐高保真原型时整理的后端接口缺失/待对齐记录）
- **分支族**：`feat/backend-api-fulfillment`（worktree `backend-api-fulfillment`）
- **状态**：已批准，待写实施计划

## 1. 目标与范围

实现 `docs/backend-api.md` 中 **B1–B5** 的全部待补接口。A 节（路径对齐）已在前一 PR
完成，C 节（误列项）经核验均已实现，**均不在本范围内**。

范围边界（已与用户确认）：

- **全部 B1–B5** 落地。
- **实时客服仅保留占位标志**：不建 WebSocket 实时系统；notify-service 已有
  `CustomerMessageController`（`/messages` + `/admin/messages` + reply）作为载体，
  前端按现有轮询消费即可。
- **新领域全部折叠进现有服务**：零新增 Maven 模块、Nacos 注册、docker-compose
  服务、Gateway 路由结构变更。

所有新增端点遵循仓库约定：**写操作返 `R<Void>`、读操作返 `*VO`/`PageDTO<T>`/
`List<*>`**；跨服务取数走 `hm-api` 的 Feign 客户端，不直连 HTTP。

## 2. 承载原则（端点 → 服务归属）

按"端点的领域归属"折叠进最贴近的现有服务：

| 服务 | 承接的文档项 | 新增表 |
|---|---|---|
| **user-service** | B1 管理端用户管理、B5 管理端个人中心/权限 | 无（`user.status` 已存在） |
| **item-service** | B2 评价列表 + 批量 + 相关推荐、B4 轮播图/广告位/秒杀 | `banner`、`seckill` |
| **trade-service** | B3 运费/可用券/物流/退款审核/导出、B4 数据看板聚合 | `logistics_trace` |
| **notify-service** | B4 在线客服占位 + `GET /faqs` | `faq` |

## 3. 现状核对结论（修正文档过时点）

实现前已核对现有代码，以下结论覆盖 `docs/backend-api.md` 中的过时描述：

- **`user` 表已有 `status` 字段**（PO 用 `UserStatus` 枚举：`1 正常 / 2 冻结`）。
  文档 B1 "需在 user 表加 status 字段"已过时——**无需 migration**，启停用直接
  复用现有字段，状态值用 **1/2**（而非 0/1）。
- **`GET /users/{id}` 现返回裸 `User`（含 `password`）**。管理端用户详情若复用它
  会泄露密码，**必须新增脱敏 `UserVO`**，不含 `password`。
- 现有部分读端点返裸 entity（如 `List<Coupon>`、`List<ItemReview>`），新增端点
  一律改用 `*VO`/`PageDTO<T>` 约定形态；**不修改既有 endpoint 形态**（避免破坏
  现网前端调用）。
- **notify-service `CustomerMessageController` 已存在**，客服占位仅做收尾。

## 4. 拆期策略

按**服务边界**拆成 4 个有序子项目，每个一条独立分支 → PR（迁移隔离、CI 绿、
review 各自闭环）。Spec 单一总文档（本文件），由 writing-plans 据此产出**分期
实施计划**（每期一个 PR）。

| Phase | 服务 | 内容 | 优先级 |
|---|---|---|---|
| 1 | user-service | B1 `/admin/users` 列表/状态/详情 + B5 profile/permissions | **P1（前端已接线 404，最高）** |
| 2 | item-service | B2 评价/批量/相关推荐 + B4 轮播图/广告位/秒杀 | P1（评价）+ P2 |
| 3 | trade-service | B3 运费/可用券/物流/退款审核/导出 + B4 数据看板 | P2 |
| 4 | notify-service | 客服占位收尾 + FAQ | P2 |

## 5. 端点契约

通用：`page` 默认 1、`size` 默认 10；分页读返 `PageDTO<T>`；列表读返 `List<*VO>`；
写返 `R<Void>`。`/admin/*` 端点依赖 Gateway 注入的身份做鉴权。

### Phase 1 · user-service

| 端点 | 返回 | 要点 |
|---|---|---|
| `GET /admin/users?page&size&keyword` | `PageDTO<UserVO>` | keyword 模糊匹配 username/phone；`UserVO` 不含 password |
| `PUT /admin/users/{id}/status`（body `{status}`） | `R<Void>` | 复用现有 `status` 字段，值 1/2 |
| `GET /admin/users/{id}` | `UserVO` | 脱敏详情 |
| `PUT /users/profile`（B5 复用，需确认 adminToken 可用） | `R<Void>` | 管理员改信息/密码；若 adminToken 不通则新增 `/admin/profile` |
| `GET /admin/profile/permissions` | `List<String>` | 基于当前管理员角色派生权限码 |

### Phase 2 · item-service

| 端点 | 返回 | 要点 |
|---|---|---|
| `GET /admin/reviews?page&size&rating` | `PageDTO<ReviewVO>` | rating 可选过滤 |
| `PUT /admin/items/status`（body `{ids,status}`） | `R<Void>` | 批量上下架 |
| `DELETE /admin/items`（body `{ids}`） | `R<Void>` | 批量删除（逻辑删） |
| `GET /items/{id}/related` | `List<ItemVO>` | 同分类取 N 条，排除自身 |
| `GET /banners` | `List<BannerVO>` | 前台：启用 + 按 sort 排序 |
| `GET /admin/banners` + CRUD + 排序 | 读 `PageDTO`/`List`，写 `R<Void>` | 管理端轮播图维护 |
| `GET /ads` | `List<BannerVO>` | 由 `banner` 表按 `type`/`position` 过滤派生（见决策①） |
| `GET /seckill/active` | `List<SeckillVO>` | 含 `endTime`（倒计时）、库存百分比（`sold`/`stock`） |

### Phase 3 · trade-service

| 端点 | 返回 | 要点 |
|---|---|---|
| `GET /orders/freight?addressId&amount` | `FreightVO` | 规则：满额包邮 + 按地区固定运费 |
| `GET /my-coupons/available?amount` | `List<CouponVO>` | 过滤满减门槛 / 有效期 / 未使用 |
| `GET /orders/{id}/logistics` | `List<LogisticsTraceVO>` | 来源 `logistics_trace` 表（见决策②） |
| `PUT /admin/orders/{id}/refund-audit`（body `{approved,reason}`） | `R<Void>` | 审核通过/驳回 |
| `GET /admin/orders/export?...` | CSV 文本流（`text/csv`） | 见决策③ |
| `GET /admin/dashboard/summary` | `DashboardSummaryVO` | 成交额/订单数/新增用户/访客 |
| `GET /admin/dashboard/trend?days` | `List<TrendPointVO>` | 时间序列趋势 |
| `GET /admin/dashboard/category-share` | `List<CategoryShareVO>` | 品类占比 |
| `GET /admin/dashboard/top-items` | `List<TopItemVO>` | 热销 TOP5 |
| `GET /admin/dashboard/todo` | `DashboardTodoVO` | 待办（待发货/待退款等计数） |
| `GET /admin/dashboard/latest-orders` | `List<OrderVO>` | 最新订单 |

数据看板经 `hm-api` Feign 拉取 user-service（新增用户数）/ item-service（品类、
商品信息）真实取数。**访客数无埋点 → 返 0 / 占位字段**，前端照常展示。

### Phase 4 · notify-service

| 端点 | 返回 | 要点 |
|---|---|---|
| 客服收发 | 复用现有 `/messages`、`/admin/messages` | 保留占位标志，前端轮询消费 |
| `GET /faqs` | `List<FaqVO>` | 来源 `faq` 表 |

## 6. 数据模型（建模决策）

新表均用 SQL 迁移脚本（不可改写既有），放 `docs/sql/` 对应服务；逻辑删 + 创建/
更新时间列遵循现有 PO 风格。

### 决策①：ads 复用 banner 表 + faq 建表

`banner` 表加 `type`/`position` 字段统一承载轮播图与广告位，一套 CRUD 复用；
`GET /ads` 按 `type`/`position` 过滤派生。`faq` 单独建表，可管理端维护。

```
banner(id, image_url, link_url, title, type, position, sort, status,
       create_time, update_time)
  -- type 区分 轮播图 / 广告位；position 标识广告位槽位
```

### 决策②：新增 logistics_trace 表

独立物流轨迹表，发货流程写入；`GET /orders/{id}/logistics` 按 `order_id` 读取
并按时间排序。

```
logistics_trace(id, order_id, node, description, trace_time, create_time)
```

### 决策③：订单导出 = CSV 文本流

`text/csv` 直接写 response，零新增依赖，Excel 可直接打开。

### 其余新表

```
seckill(id, item_id, seckill_price, start_time, end_time, stock, sold, status,
        create_time, update_time)
faq(id, question, answer, sort, status, create_time, update_time)
```

## 7. 横切关注点

**错误处理**：沿用 `CommonExceptionAdvice` 统一兜底；参数校验用 `@Validated` +
DTO；`/admin/*` 端点依赖 Gateway 注入身份，越权/未登录抛业务异常由 advice 包
`R<Void>`。

**Gateway 路由**：不破坏现有结构。`/admin/*`、`/banners`、`/seckill`、`/ads`、
`/faqs` 走现有转发规则；前台 GET 若需免登，在对应服务侧处理，**不动 Gateway**。

**测试（80% 门槛，三层）**：

1. 单元：每个新 service 方法 JUnit5 + Mockito（运费规则、可用券过滤、看板聚合、
   分页/脱敏）。
2. 集成：新端点 Spring Boot Test + Testcontainers（MySQL/Redis）跑控制器 → DB；
   JaCoCo 已抬到 80%。
3. 冒烟：`scripts/smoke/` 加新端点 HTTP 校验。

**KB / harness 义务（每期 PR 内）**：

- `docs/agent-harness/tasks/active/` 建任务记录（context/verification/audit/handoff
  四叙述文件 + `task.yaml`，`status: active`）；改 Feign / 新表需登记。
- 改动 tracks 路径必须同步对应 `docs/knowledge-base/modules/*.md`（K005）；新端点
  契约写进对应模块页。
- 跨服务取数（看板）新增 `hm-api` Feign 客户端 + fallback。
- 完成后 `agent_harness.py check` / `knowledge_base.py check` / `engineering-lint.py`
  全绿。

**交付节奏**：4 期各自从 `main` 切 `task/YYYY-MM-DD-<slug>` 分支 → 本地验收 →
推远程 → 开 PR → 过 CI（含 codex-review 强门控）→ 合并后删远程分支。

## 8. 未决/确认项

- B5 管理员改密码/信息是否可直接复用 `PUT /users/profile`（adminToken 是否被
  user-service 接受）——Phase 1 实现时先验证，不通则新增 `/admin/profile`。
- `docs/api.md` §12.1 把 `searchItems` 误记为 `/items/search`，A 节对齐后需同步
  修订（文档勘误，可在任一期顺带处理）。
