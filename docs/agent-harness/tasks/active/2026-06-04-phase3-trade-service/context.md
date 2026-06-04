# Context: Phase 3 — trade-service Backend API Fulfillment

## Objective

实现 Backend API Fulfillment spec 的 Phase 3（trade-service），共 11 个端点：
运费查询、可用优惠券、物流轨迹、退款审核、订单导出 CSV、数据看板（6 个端点）。

## Scope

- In scope: trade-service 新增/扩展端点，logistics_trace 表，DashboardService，相关 VO/DTO。
- Out of scope: user-service / item-service / notify-service（Phase 1/2/4）、Gateway 路由变更。

## Changes Made

### 新增表
- `logistics_trace`：物流轨迹表（id, order_id, node, description, trace_time, create_time）。

### 新增 PO / Mapper / Service
- `LogisticsTrace` PO + `LogisticsTraceMapper` + `ILogisticsTraceService` / `LogisticsTraceServiceImpl`。
- `IDashboardService` / `DashboardServiceImpl`：数据看板聚合服务。

### 新增 VO / DTO
- `FreightVO`、`CouponVO`、`LogisticsTraceVO`。
- `DashboardSummaryVO`、`TrendPointVO`、`CategoryShareVO`、`TopItemVO`、`DashboardTodoVO`。
- `RefundAuditDTO`（退款审核请求体）。

### 扩展现有服务
- `ICouponService.getAvailableCouponsForAmount(userId, amount)`：过滤满减门槛 + 有效期 + 未使用。
- `IOrderService.refundAudit(orderId, approved, reason)`：退款审核（通过→关闭，驳回→恢复原状态）。

### 新增/扩展端点
| 端点 | Controller | 返回 |
|---|---|---|
| `GET /orders/freight?addressId&amount` | OrderController | FreightVO |
| `GET /my-coupons/available?amount` | CouponController | List<CouponVO> |
| `GET /orders/{id}/logistics` | OrderController | List<LogisticsTraceVO> |
| `PUT /admin/orders/{id}/refund-audit` | OrderController | R<Void> |
| `GET /admin/orders/export?...` | OrderController | CSV text/csv |
| `GET /admin/dashboard/summary` | DashboardController | DashboardSummaryVO |
| `GET /admin/dashboard/trend?days` | DashboardController | List<TrendPointVO> |
| `GET /admin/dashboard/category-share` | DashboardController | List<CategoryShareVO> |
| `GET /admin/dashboard/top-items` | DashboardController | List<TopItemVO> |
| `GET /admin/dashboard/todolist`（管理端待办计数，原路径含 t-d-l-s-t 五字母段） | DashboardController | DashboardTodoVO |
| `GET /admin/dashboard/latest-orders` | DashboardController | List<OrderVO> |

### Feign 扩展
- `UserClient.countNewUsers(days)`：数据看板获取新增用户数（user-service Phase 1 实现前返回 0）。

### 测试
- `DashboardServiceImplTest`：14 个测试覆盖全部 6 个看板方法。
- `OrderServiceImplTest`：新增 5 个 refundAudit 测试。
- `CouponServiceImplTest`：新增 5 个 getAvailableCouponsForAmount 测试。

## Coverage

| 类 | 分支覆盖率 |
|---|---|
| CouponServiceImpl | 100% (14/14) |
| OrderServiceImpl | 90% (36/40) |
| DashboardServiceImpl | 75% (21/28) |
| **service.impl 合计** | **86.6% (71/82)** |

## Related Artifacts

- Spec: `docs/superpowers/specs/2026-06-04-backend-api-fulfillment-design.md`
- Plan: `docs/superpowers/plans/2026-06-04-phase3-trade-service.md`
- SQL: `docs/sql/trade-service-logistics.sql`
