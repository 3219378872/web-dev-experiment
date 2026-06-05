---
title: order-checkout-flow
tracks:
  - trade-service/
  - cart-service/
  - item-service/
  - pay-service/
last_synced_commit: ee308f3
last_synced_date: 2026-06-05
sync_note: "修复订单详情缺少商品信息，无需文档更新"
---

# order-checkout-flow

C 端用户从加购到支付完成的端到端流程，跨 [cart-service](../modules/cart-service.md)、
[item-service](../modules/item-service.md)、[trade-service](../modules/trade-service.md)、
[pay-service](../modules/pay-service.md)，RabbitMQ 负责下单后清车、支付成功回写、
订单状态通知与超时关单副作用。

## 流程

1. **加购** —— [hmall-web](../modules/hmall-web.md) → [hm-gateway](../modules/hm-gateway.md)
   → [cart-service](../modules/cart-service.md) `POST /carts`。
   写入 `cart` 表；不校验库存（库存仅在下单时校验）。
2. **进入结算** —— hmall-web 取购物车勾选项 + 用户地址（user-service）+ 可用券
   （trade-service）合并展示。前端持有订单草稿（仅前端态）。
3. **创建订单（核心，Seata AT 全局事务）** —— `POST /orders`：
   - `createOrder` 标 `@GlobalTransactional`，trade-service 作为 TM 开启全局事务。
   - trade-service 查询商品并按服务端价格计算总价。
   - 写 `order` + `order_detail`（trade 本地 RM 分支）。
   - 同步调 [item-service](../modules/item-service.md) 扣减库存（item RM 分支，XID 经
     Feign 头传播）；**任一分支失败，AT 用各库 `undo_log` 反向补偿**：恢复库存、回滚订单。
   - 全局事务提交后发布 `trade.topic/order.create`，由 cart-service 异步删除对应购物车项，
     notify-service 生成下单成功站内信。清车/通知是 MQ 异步副作用，**不入全局事务**。
   - 事务提交后发布 `delay.exchange/order.delay`，30 分钟后进入 `order.close.queue`
     通过带 `status = 待支付` 条件的原子更新关闭仍未支付的订单。
4. **支付** —— hmall-web 跳支付页 → [pay-service](../modules/pay-service.md)
   `POST /pay-orders/{id}/pay`。模拟支付通道直接打标已支付。
   余额支付（`tryPayOrderByBalance`）同样标 `@GlobalTransactional`：pay-service 作为 TM
   协调 user 的 `deductMoney`（RM）+ 本地改支付单，扣款失败则余额与支付单一致回滚。
5. **支付回写** —— pay-service 支付全局事务提交后发布 `pay.topic/pay.success`，
   trade-service 消费后仅把待支付订单置为"已支付"并记录支付时间（MQ 异步，全局事务外）。
6. **发货 / 退款** —— 管理端 [hmall-admin](../modules/hmall-admin.md) 操作发货或处理退款，
   对应订单状态机推进，并发布 `trade.topic/order.status.{shipped,refund,cancel}` 给
   [notify-service](../modules/notify-service.md)。

## 关键不变量

- 库存扣减发生在订单创建事务内，绝不允许"先下单后异步扣库存"。
- 支付成功回写是最终一致链路；trade-service 的消费逻辑必须可重复执行，重复/过期
  `pay.success` 不得改变已关闭、已取消或已退款订单。
- 超时关单必须和支付成功回写一样带状态条件原子更新；延时消息与 `pay.success` 并发时，
  只能有仍处于待支付状态的一方被推进。
- 订单创建事件携带 `userId` 与 `itemIds`，cart-service listener 必须显式写入
  `UserContext` 后再清车。
- 价格以服务端商品当前价为准；前端展示价仅作显示，提交时不信任。
- 金额单位全链路用分（long），不允许 double。

## 失败与恢复

- 库存不足：trade-service 返回 4xx，前端友好提示。
- MQ 发送失败：`RabbitMqMessagePublisher` 尝试把消息写入 `mq_outbox_message`，
  作为后续重试/排查入口；业务事务内的消息在提交后才发送。
- 支付超时：订单创建时投递延时消息，到期后用原子条件更新仅关闭仍处于待支付状态的订单。
- 消费失败：manual nack 先进入专用 retry queue，TTL 到期后回到原队列；
  达到最大重试次数后转入 `hmall.mq.dead.queue`。
- 支付回写丢失：依赖 outbox/死信排查与后续对账补偿。

## 测试策略

- 单元测试：service 层各方法（库存扣减、订单写入、状态机）。
- 集成测试：Testcontainers RabbitMQ 发 `pay.success`，断言 trade-service listener
  更新订单状态。
- E2E（Playwright）：从加购到模拟支付完成，断言订单状态变迁与 UI 提示。
- 冒烟：docker compose 起栈后 HTTP 验证整条流程能跑通。
