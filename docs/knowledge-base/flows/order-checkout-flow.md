---
title: order-checkout-flow
tracks:
  - trade-service/
  - cart-service/
  - item-service/
  - pay-service/
last_synced_commit: 486821c
last_synced_date: 2026-06-01
sync_note: "Phase 1 fix: item-service 测试修复，不改业务逻辑"
---

# order-checkout-flow

C 端用户从加购到支付完成的端到端流程，跨 [cart-service](../modules/cart-service.md)、
[item-service](../modules/item-service.md)、[trade-service](../modules/trade-service.md)、
[pay-service](../modules/pay-service.md)，由 Seata 协调分布式事务。

## 流程

1. **加购** —— [hmall-web](../modules/hmall-web.md) → [hm-gateway](../modules/hm-gateway.md)
   → [cart-service](../modules/cart-service.md) `POST /carts`。
   写入 `cart` 表；不校验库存（库存仅在下单时校验）。
2. **进入结算** —— hmall-web 取购物车勾选项 + 用户地址（user-service）+ 可用券
   （trade-service）合并展示。前端持有订单草稿（仅前端态）。
3. **创建订单（核心）** —— `POST /orders`：
   - trade-service 开启 Seata 全局事务（TM）。
   - 调 [item-service](../modules/item-service.md) `扣减库存`（RM, 失败即回滚）。
   - 写 `order` + `order_detail`。
   - 调 [cart-service](../modules/cart-service.md) 删除勾选项。
   - 调 [pay-service](../modules/pay-service.md) 创建 `pay_order`（未支付）。
   - 全部成功 → 全局提交；任一失败 → 全局回滚，库存回滚、订单/支付单/购物车回滚。
4. **支付** —— hmall-web 跳支付页 → [pay-service](../modules/pay-service.md)
   `POST /pay-orders/{id}/pay`。模拟支付通道直接打标已支付。
5. **支付回写** —— pay-service 通过 Feign 或 MQ 通知 trade-service 把订单状态
   置为"已支付"。trade-service 触发后续通知（[notify-service](../modules/notify-service.md)）。
6. **发货 / 退款** —— 管理端 [hmall-admin](../modules/hmall-admin.md) 操作发货或处理退款，
   对应订单状态机推进。

## 关键不变量

- 库存扣减发生在订单创建事务内，绝不允许"先下单后异步扣库存"。
- 订单与支付单一对一；订单号 ≠ 支付单号；前端只显示订单号。
- Seata TCC 的 Try/Confirm/Cancel 必须幂等。
- 价格以服务端商品当前价为准；前端展示价仅作显示，提交时不信任。
- 金额单位全链路用分（long），不允许 double。

## 失败与恢复

- 库存不足：trade-service 返回 4xx，前端友好提示。
- Seata 回滚：所有副作用（库存、订单、支付单、购物车）原子撤销。
- 支付超时：定时任务扫描"未支付且超时"订单，回滚库存与订单。
- 支付回调丢失：定时对账（pay-service ↔ 支付通道）兜底。

## 测试策略

- 单元测试：service 层各方法（库存扣减、订单写入、状态机）。
- 集成测试：Testcontainers 起 MySQL + Seata，断言事务回滚正确性。
- E2E（Playwright）：从加购到模拟支付完成，断言订单状态变迁与 UI 提示。
- 冒烟：docker compose 起栈后 HTTP 验证整条流程能跑通。
