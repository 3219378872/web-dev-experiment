---
title: trade-service
tracks:
  - trade-service/
last_synced_commit: c03b47b
last_synced_date: 2026-06-01
sync_note: "Phase 1 测试补足完成：新增 OrderServiceImplTest(11) + CouponServiceImplTest(7)，不改业务逻辑"
---

# trade-service

## 职责

订单与订单项、优惠券领取/使用、退款、发货、订单管理端操作。
作为下单事务的协调方，跨 [cart-service](cart-service.md)、[item-service](item-service.md)、
[pay-service](pay-service.md) 调度 Seata 分布式事务。

## 公开接口与契约

- HTTP API（C 端）：`/orders`（创建/查询/取消）、`/coupons/**`（领取/使用/列表）。
- HTTP API（管理端）：`/admin/orders/**`（列表/详情/发货）、`/admin/coupons/**`、
  `/admin/refunds/**`。
- 对外 Feign：[hm-api](hm-api.md) `TradeClient` 暴露订单查询给其它服务。
- 持久层：`hm-trade` 数据库；表 `order`、`order_detail`、`order_logistics`、
  `coupon`、`user_coupon`。
- Seata：作为 TM/RM 协调下单/支付/库存事务（TCC 或 AT）。

## 上游

- [hm-gateway](hm-gateway.md) 转发外部请求。
- [hmall-web](hmall-web.md) C 端下单、[hmall-admin](hmall-admin.md) 管理端订单处理。
- [pay-service](pay-service.md) 支付回调通知订单完成。

## 下游

- MySQL（`hm-trade`）。
- 调用 [cart-service](cart-service.md) 拉勾选项、删勾选项。
- 调用 [item-service](item-service.md) 校验/扣减库存、查商品摘要。
- 调用 [pay-service](pay-service.md) 创建支付单。
- 调用 [user-service](user-service.md) 取收货地址。

## 关键文件

- `TradeApplication.java`、`controller/OrderController.java`、
  `controller/CouponController.java`。
- `service/IOrderService.java` / `ICouponService.java` / `IUserCouponService.java` 与 impl。
- `mapper/OrderMapper.java` / `OrderDetailMapper.java` / `CouponMapper.java` /
  `UserCouponMapper.java` / `OrderLogisticsMapper.java`。

## 注意事项与陷阱

- 下单必须在 Seata 全局事务内：库存扣减、订单写入、购物车清理、支付单创建。
- 优惠券面额校验放服务端，前端传值仅作展示。
- 退款必须先核对状态机：仅 `已支付`/`已发货` 可发起；不可对未支付订单退款。
- 管理端发货操作必须幂等。
- 订单号生成用雪花/号段，禁止自增 ID 暴露给前端。

详见 [order-checkout-flow](../flows/order-checkout-flow.md)。
