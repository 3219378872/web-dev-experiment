---
title: pay-service
tracks:
  - pay-service/
last_synced_commit: bc09d0e
last_synced_date: 2026-05-26
sync_note: ""
---

# pay-service

## 职责

支付单生命周期：创建、模拟支付、对账回调、状态变更通知。Seata 全局事务参与方。

## 公开接口与契约

- HTTP API：`/pay-orders`（创建/查询/取消）、`/pay-orders/{id}/pay`（模拟支付）。
- 对外 Feign：[hm-api](hm-api.md) `PayClient` 暴露支付单查询/创建。
- 持久层：`hm-pay` 数据库；表 `pay_order`。
- 支付完成后通过 RabbitMQ / 直接 Feign 通知 [trade-service](trade-service.md) 更新订单状态。

## 上游

- [trade-service](trade-service.md) 下单时创建支付单。
- [hm-gateway](hm-gateway.md) 转发外部请求（前端模拟支付）。

## 下游

- MySQL（`hm-pay`）。
- 调用 [trade-service](trade-service.md) 回写订单已支付。

## 关键文件

- `PayApplication.java`、`controller/PayController.java`。
- `service/IPayOrderService.java` 与 impl。
- `domain/po/PayOrder.java`。
- `enums/PayType.java` / `PayChannel.java` / `PayStatus.java`。
- `mapper/PayOrderMapper.java`。

## 注意事项与陷阱

- 支付回调必须幂等：同一支付单多次回调要返回同样结果。
- 状态机：`未支付` → `已支付` 单向；不允许逆向。
- 模拟支付要标注是测试通道，避免与生产对账混淆。
- 金额单位统一用分（long），不要用 double。
