# Verification

## Test Results

| Command | Result | Evidence |
| --- | --- | --- |
| `mvn -B -ntp -pl trade-service verify -DskipIntegrationTests` | BUILD SUCCESS | JaCoCo 分支覆盖率 90.5% (38/42)，门控 0.80 通过 |
| `mvn -B -ntp verify -DskipIntegrationTests` | BUILD SUCCESS | 全模块构建通过，80% 门控全部满足 |

## Coverage Summary

| 模块 | 分支覆盖率 | 状态 |
|------|-----------|------|
| trade-service | 90.5% (38/42) | ✓ |
| item-service | 100.0% (2/2) | ✓ |
| cart-service | 91.7% (11/12) | ✓ |
| user-service | 97.1% (33/34) | ✓ |
| pay-service | 91.7% (11/12) | ✓ |
| file-service | 100.0% (2/2) | ✓ |

## New Tests Added (trade-service)

1. `CouponServiceImplTest.claimCoupon_disabled_throwsBadRequest` — 覆盖优惠券 status != 1 分支
2. `OrderServiceImplTest.cancelOrder_nonExistentOrder_throwsBadRequest` — 覆盖订单不存在分支
3. `OrderServiceImplTest.confirmReceive_nonExistentOrder_throwsBadRequest` — 覆盖订单不存在分支
4. `OrderServiceImplTest.refund_nonExistentOrder_throwsBadRequest` — 覆盖订单不存在分支
5. `OrderServiceImplTest.refund_shippedOrder_refunds` — 覆盖已发货订单退款分支
6. `OrderServiceImplTest.ship_nonExistentOrder_throwsBadRequest` — 覆盖订单不存在分支

## Gate Configuration

- 根 pom.xml: `<minimum>0.80</minimum>`
- user-service/pom.xml: `<minimum>0.80</minimum>`
- pay-service/pom.xml: `<minimum>0.80</minimum>`
