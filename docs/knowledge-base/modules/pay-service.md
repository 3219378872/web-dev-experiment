---
title: pay-service
tracks:
  - pay-service/
last_synced_commit: 7b4de82
last_synced_date: 2026-06-03
sync_note: "新增单元测试 + JaCoCo skip 属性修复"
---

# pay-service

## 职责

支付单生命周期：创建、模拟支付、对账回调、状态变更通知。支付成功后发布 RabbitMQ
事件，由 trade-service 异步更新订单状态。

## 公开接口与契约

- HTTP API：`/pay-orders`（创建/查询/取消）、`/pay-orders/{id}/pay`（模拟支付）。
- 对外 Feign：[hm-api](hm-api.md) `PayClient` 暴露支付单查询/创建。
- 持久层：`hm-pay` 数据库；表 `pay_order`、`mq_outbox_message`。
- 支付完成后发布 `pay.topic/pay.success`，payload 为 `PaySuccessEvent`。

## 上游

- [trade-service](trade-service.md) 下单时创建支付单。
- [hm-gateway](hm-gateway.md) 转发外部请求（前端模拟支付）。

## 下游

- MySQL（`hm-pay`）。
- RabbitMQ `pay.topic`，routing key `pay.success`。

## 关键文件

- `PayApplication.java`、`controller/PayController.java`。
- `service/IPayOrderService.java` 与 impl。
- `domain/po/PayOrder.java`。
- `enums/PayType.java` / `PayChannel.java` / `PayStatus.java`。
- `mapper/PayOrderMapper.java`。

## 注意事项与陷阱

- `tryPayOrderByBalance` 是 Seata AT 全局事务入口（`@GlobalTransactional` + `@Transactional`），
  作为 TM 协调 user 的 `deductMoney`（RM）+ 本地改支付单。扣款失败时余额与支付单一致回滚。
  改订单状态走 `pay.success` MQ 异步，落在全局事务之外。Seata 客户端默认
  `seata.enabled=false`，单测不依赖 Seata Server。
- 支付回调必须幂等：同一支付单多次回调要返回同样结果。
- 状态机：`未支付` → `已支付` 单向；不允许逆向。
- `tryPayOrderByBalance` 不再直接调用 `OrderClient.updateById`；订单状态由
  [trade-service](trade-service.md) 的 `pay.success` listener 异步推进。事件通过
  [hm-common](hm-common.md) `MqMessagePublisher` 在支付事务提交后发送。
- 模拟支付要标注是测试通道，避免与生产对账混淆。
- 金额单位统一用分（long），不要用 double。
