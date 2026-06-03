---
title: trade-service
tracks:
  - trade-service/
last_synced_commit: 87b1acb30b772f96d3afe2f0e9aaa2e5aeaa4c5c
last_synced_date: 2026-06-03
sync_note: "JaCoCo 门控 70%→80%，trade-service 测试增强（仅测试和配置改动，KB 内容不变）"
---

# trade-service

## 职责

订单与订单项、优惠券领取/使用、退款、发货、订单管理端操作。
作为下单链路协调方，同步校验商品并扣库存，通过 RabbitMQ 发布下单后副作用事件。

## 公开接口与契约

- HTTP API（C 端）：`/orders`（创建/查询/取消）、`/coupons/**`（领取/使用/列表）。
- HTTP API（管理端）：`/admin/orders/**`（列表/详情/发货）、`/admin/coupons/**`、
  `/admin/refunds/**`。
- 对外 Feign：[hm-api](hm-api.md) `TradeClient` 暴露订单查询给其它服务。
- 持久层：`hm-trade` 数据库；表 `order`、`order_detail`、`order_logistics`、
  `coupon`、`user_coupon`、`mq_outbox_message`。
- RabbitMQ：
  - 发布 `trade.topic/order.create` 给 cart 清车、notify 站内信。
  - 发布 `delay.exchange/order.delay`，30 分钟 TTL 后转为 `order.close` 关单。
  - 消费 `pay.topic/pay.success`，仅把待支付订单改为已支付。
  - 发布 `trade.topic/order.status.{shipped,refund,cancel}` 给 notify。

## 上游

- [hm-gateway](hm-gateway.md) 转发外部请求。
- [hmall-web](hmall-web.md) C 端下单、[hmall-admin](hmall-admin.md) 管理端订单处理。
- [pay-service](pay-service.md) 通过 `pay.success` 事件通知订单完成。

## 下游

- MySQL（`hm-trade`）。
- 调用 [cart-service](cart-service.md) 拉勾选项；下单成功后清车改为
  `order.create` 事件异步处理。
- 调用 [item-service](item-service.md) 校验/扣减库存、查商品摘要。
- 调用 [pay-service](pay-service.md) 创建支付单。
- 调用 [user-service](user-service.md) 取收货地址。
- RabbitMQ（trade/pay/delay/dead exchanges）。

## 关键文件

- `TradeApplication.java`、`controller/OrderController.java`、
  `controller/CouponController.java`。
- `service/IOrderService.java` / `ICouponService.java` / `IUserCouponService.java` 与 impl。
- `mapper/OrderMapper.java` / `OrderDetailMapper.java` / `CouponMapper.java` /
  `UserCouponMapper.java` / `OrderLogisticsMapper.java`。
- `service/impl/OrderServiceImpl.java` —— 订单创建、MQ 发布、支付成功消费、延时关单、
  状态变更发布。
- `it/RabbitMqOrderEventIT.java` —— RabbitMQ Testcontainers 支付成功闭环。

## 注意事项与陷阱

- `createOrder` 是 Seata AT 全局事务入口（`@GlobalTransactional` + `@Transactional`），
  作为 TM 协调 trade 本地存单 + item 的 `deductStock`（RM）。任一参与方失败时 AT 用
  `undo_log` 自动反向补偿。库存扣减仍是事务内同步关键路径；清车、下单通知、超时关单
  是 RabbitMQ 异步副作用，经 `MqMessagePublisher` 在事务提交后发布，**落在全局事务之外**，
  不被回滚——这是 Seata 管强一致 DB、MQ 管最终一致副作用的职责划分。
- Seata 客户端默认 `seata.enabled=false`，单测/`test` job 不依赖 Seata Server；
  compose 中四服务注入 `SEATA_ENABLED=true`。`undo_log` 为单 `hmall` 库一张（共享）。
- `markOrderPaySuccess` 既被旧 Feign 入口使用，也被 `pay.success` listener 调用；
  只允许 `待支付` → `已支付`，重复/过期 `pay.success` 不得重开已关闭、已取消或已退款订单。
- 延时关单只能原子更新仍处于 `待支付` 的订单；不得先读订单状态再无条件写回，
  避免与 `pay.success` 并发时覆盖已支付状态。
- RabbitMQ 消费失败交给 [hm-common](hm-common.md) `MqConsumerSupport`：先延迟重试，
  达到上限后进入死信队列。
- 优惠券面额校验放服务端，前端传值仅作展示。
- 退款必须先核对状态机：仅 `已支付`/`已发货` 可发起；不可对未支付订单退款。
- 管理端发货操作必须幂等，并发布 `order.status.shipped`。
- 订单号生成用雪花/号段，禁止自增 ID 暴露给前端。

详见 [order-checkout-flow](../flows/order-checkout-flow.md)。
