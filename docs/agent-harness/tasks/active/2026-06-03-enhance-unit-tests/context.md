# Context: Enhance Unit Tests

## Objective

为所有模块增强单元测试，将 JaCoCo 分支覆盖率门控从 70% 提升至 80%。

## Scope

- In scope: 所有含 service.impl 包的模块（item-service, cart-service, user-service, trade-service, pay-service, file-service）。
- Out of scope: notify-service（已被根 POM 排除在 JaCoCo 检查外）、hm-common/hm-api/hm-gateway（无 service.impl 包）。

## Changes Made

1. **JaCoCo 门控提升**: 根 pom.xml、user-service/pom.xml、pay-service/pom.xml 的 `<minimum>` 从 `0.70` 改为 `0.80`。
2. **trade-service 测试增强**:
   - `CouponServiceImplTest`: 新增 `claimCoupon_disabled_throwsBadRequest`（覆盖 status != 1 分支）。
   - `OrderServiceImplTest`: 新增 5 个测试覆盖 null 订单和已发货退款分支：
     - `cancelOrder_nonExistentOrder_throwsBadRequest`
     - `confirmReceive_nonExistentOrder_throwsBadRequest`
     - `refund_nonExistentOrder_throwsBadRequest`
     - `refund_shippedOrder_refunds`
     - `ship_nonExistentOrder_throwsBadRequest`

## Coverage Results

| 模块 | 门控前 | 门控后 |
|------|--------|--------|
| trade-service | 76.2% (32/42) | 90.5% (38/42) |
| item-service | 100% (2/2) | 100% (2/2) |
| cart-service | 91.7% (11/12) | 91.7% (11/12) |
| user-service | 97.1% (33/34) | 97.1% (33/34) |
| pay-service | 91.7% (11/12) | 91.7% (11/12) |
| file-service | 100% (2/2) | 100% (2/2) |

## Related Artifacts

- Spec: none — 测试增强任务，无需独立 spec。
- Plan: none — 测试增强任务，无需独立 plan。

## Likely Files

- `pom.xml`（根 JaCoCo 门控 0.70 → 0.80）
- `user-service/pom.xml`（JaCoCo 门控 0.70 → 0.80）
- `pay-service/pom.xml`（JaCoCo 门控 0.70 → 0.80）
- `trade-service/src/test/java/com/hmall/service/impl/CouponServiceImplTest.java`
- `trade-service/src/test/java/com/hmall/service/impl/OrderServiceImplTest.java`

## Safety Constraints

- 不在源码中硬编码密钥/口令/Token。
- 不破坏现有公共 API 响应封装。
- 测试使用 H2 内存数据库，不依赖外部服务。

## Worktree Or Branch

- `feat/enhance-tests`
