# Phase 3 Implementation Plan — trade-service

**Spec**: `docs/superpowers/specs/2026-06-04-backend-api-fulfillment-design.md`
**Branch**: `task/2026-06-04-phase3-trade-service`

## 1. Data Model

| Item | File | Description |
|---|---|---|
| LogisticsTrace PO | `domain/po/LogisticsTrace.java` | 物流轨迹 PO |
| LogisticsTraceMapper | `mapper/LogisticsTraceMapper.java` | MyBatis-Plus mapper |
| SQL migration | `docs/sql/trade-service-logistics.sql` | logistics_trace 建表 |
| Test schema | `src/test/resources/schema.sql` + `sql/schema.sql` | H2 测试表 |

## 2. VOs / DTOs

| Class | Description |
|---|---|
| FreightVO | 运费信息（金额 + 是否包邮） |
| CouponVO | 优惠券脱敏 VO |
| LogisticsTraceVO | 物流轨迹 VO |
| RefundAuditDTO | 退款审核请求体（approved + reason） |
| DashboardSummaryVO | 看板概览 |
| TrendPointVO | 趋势数据点 |
| CategoryShareVO | 品类占比 |
| TopItemVO | 热销 TOP |
| DashboardTodoVO | 待办计数 |

## 3. Service Layer

| Method | Service | Description |
|---|---|---|
| getAvailableCouponsForAmount | ICouponService | 按金额过滤可用优惠券 |
| refundAudit | IOrderService | 退款审核（通过/驳回） |
| getSummary / getTrend / getCategoryShare / getTopItems / getTodo / getLatestOrders | IDashboardService | 数据看板 6 个聚合方法 |

## 4. Controller Layer

| Endpoint | Controller | Return |
|---|---|---|
| GET /orders/freight | OrderController | FreightVO |
| GET /orders/{id}/logistics | OrderController | List<LogisticsTraceVO> |
| PUT /admin/orders/{id}/refund-audit | OrderController | R<Void> |
| GET /admin/orders/export | OrderController | CSV |
| GET /my-coupons/available | CouponController | List<CouponVO> |
| GET /admin/dashboard/* (6 endpoints) | DashboardController | various VOs |

## 5. Feign

| Method | Client | Description |
|---|---|---|
| countNewUsers(days) | UserClient | 数据看板新增用户数 |

## 6. Tests

- DashboardServiceImplTest: 14 tests
- OrderServiceImplTest: +5 refundAudit tests
- CouponServiceImplTest: +5 getAvailableCouponsForAmount tests
- Total: 52 tests, all passing
- JaCoCo service.impl branch coverage: 86.6% > 80% gate
