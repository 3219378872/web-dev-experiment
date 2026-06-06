---
title: order-checkout-flow
tracks:
  - trade-service/
  - cart-service/
  - item-service/
  - pay-service/
last_synced_commit: 4a14a2b
last_synced_date: 2026-06-06
sync_note: "fix(#139): item-service 本次只新增管理端评价 rating 区间过滤，不改变下单扣库存、购物车清理、支付或订单状态流。"
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

## 订单与优惠券展示契约

- C 端订单列表 `GET /orders` 支持 `page`、`size`、`status`、`keyword`；`keyword`
  可匹配订单号或订单明细商品名，响应 `OrderVO.details` 供前端展示、再次购买和评价入口使用。
- 退款/售后使用订单状态 `6`。C 端已付款/已发货订单调用 `POST /orders/{id}/refund`
  后进入退款处理中；状态 `5` 仍表示交易关闭/已取消。
- 管理端订单列表与 CSV 导出支持 `orderId`、`userId`、`status` 过滤；订单详情页物流
  复用 `GET /orders/{id}/logistics`，退款审核对 `status=6` 订单调用
  `PUT /admin/orders/{id}/refund-audit`。
- 券包展示 `GET /my-coupons` 返回 `UserCouponVO`，包含券面信息和 `userCouponStatus`、
  `usedOrderId`、`useTime`、`claimTime`。下单结算可用券继续使用
  `GET /my-coupons/available?amount`，避免把已使用/已过期券混入结算选择。
