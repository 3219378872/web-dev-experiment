---
title: order-checkout-flow
tracks:
  - trade-service/
  - cart-service/
  - item-service/
  - pay-service/
last_synced_commit: d418693
last_synced_date: 2026-06-05
sync_note: "Issue #92: cart-service 修复商品数据填充，下单结算流程本身未变，无需内容更新"
---

# order-checkout-flow

## 职责

覆盖从购物车勾选结算到支付成功的端到端流程。

## 核心流程

1. 用户在 `hmall-web` 购物车页勾选商品，点击"去结算"。
2. `hmall-web` 调用 `trade-service` `POST /orders` 创建订单。
3. `trade-service`:
   - `createOrder` 标 `@GlobalTransactional`，trade-service 作为 TM 开启全局事务。
   - trade-service 查询商品并按服务端价格计算总价。
   - 写 `order` + `order_detail`（trade 本地 RM 分支）。
   - 同步调 [item-service](../modules/item-service.md) 扣减库存（item RM 分支，XID 经
     Seata 代理传播）。
4. 全局事务提交后发布 `trade.topic/order.create`，由 cart-service 异步删除对应购物车项，
   [notify-service](../modules/notify-service.md) 发送站内信。
5. 事务提交后发布 `delay.exchange/order.delay`，30 分钟后进入 `order.close.queue`
   自动关单（若仍未支付）。
6. 用户在前端确认支付，调 [pay-service](../modules/pay-service.md) `POST /pay-orders`
   生成支付单，再 `POST /pay-orders/{id}` 完成余额支付。
7. pay-service 支付成功后发 MQ `pay.topic/pay.success`，trade-service 消费后更新
   订单状态为已付款。
