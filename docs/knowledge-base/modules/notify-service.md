---
title: notify-service
tracks:
  - notify-service/
last_synced_commit: b25e21464938f9ce5f1cef682ae90224ca30f054
last_synced_date: 2026-06-02
sync_note: "同步订单事件消费失败重试语义"
---

# notify-service

## 职责

站内通知、客服消息、用户反馈。后端各业务事件（下单成功/发货/退款/系统公告）
最终在这里落库，前端拉取展示。

## 公开接口与契约

- HTTP API：`/notifications/**`、`/customer-messages/**`、`/feedbacks/**`。
- 持久层：`hm-notify` 数据库；表 `notification`、`customer_message`、`feedback`。
- RabbitMQ：
  - 消费 `trade.topic/order.create` 到 `notify.order.create.queue`，生成下单成功站内信。
  - 消费 `trade.topic/order.status.*` 到 `notify.order.status.queue`，生成发货/退款/取消站内信。

## 上游

- [hm-gateway](hm-gateway.md) 转发外部请求（前端拉消息）。
- [trade-service](trade-service.md) 通过 RabbitMQ 推送下单与订单状态事件。

## 下游

- MySQL（`hm-notify`）。

## 关键文件

- `NotifyApplication.java`、`controller/NotificationController.java`、
  `controller/CustomerMessageController.java`、`controller/FeedbackController.java`。
- `service/INotificationService.java` 与 impl。
- `mapper/NotificationMapper.java` / `CustomerMessageMapper.java` / `FeedbackMapper.java`。
- `mq/OrderEventListener.java` —— 订单创建与状态事件消费入口。

## 注意事项与陷阱

- 站内信对单用户高并发推送时需异步入库 + 批量写。
- 已读/未读状态写入要走更新而非删除。
- 客服消息需要会话上下文聚合，不要按单条独立返回。
- MQ listener 保存的是 `CustomerMessage`；新增订单事件类型时要同时补充文案映射与测试。
- `OrderEventListener` 失败时通过 [hm-common](hm-common.md) `MqConsumerSupport`
  先延迟重试，超过上限后进入死信队列。
