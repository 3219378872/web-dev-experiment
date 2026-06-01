---
title: notify-service
tracks:
  - notify-service/
last_synced_commit: 4d39250
last_synced_date: 2026-06-01
sync_note: "v3 合并 PR：所有阶段测试补足共 51 tests，覆盖率 80.5%"
---

# notify-service

## 职责

站内通知、客服消息、用户反馈。后端各业务事件（下单成功/发货/退款/系统公告）
最终在这里落库，前端拉取展示。

## 公开接口与契约

- HTTP API：`/notifications/**`、`/customer-messages/**`、`/feedbacks/**`。
- 持久层：`hm-notify` 数据库；表 `notification`、`customer_message`、`feedback`。
- 可选 RabbitMQ 消费者订阅业务事件主题。

## 上游

- [hm-gateway](hm-gateway.md) 转发外部请求（前端拉消息）。
- [trade-service](trade-service.md) / [pay-service](pay-service.md)
  通过事件或 Feign 推送业务消息。

## 下游

- MySQL（`hm-notify`）。

## 关键文件

- `NotifyApplication.java`、`controller/NotificationController.java`、
  `controller/CustomerMessageController.java`、`controller/FeedbackController.java`。
- `service/INotificationService.java` 与 impl。
- `mapper/NotificationMapper.java` / `CustomerMessageMapper.java` / `FeedbackMapper.java`。

## 注意事项与陷阱

- 站内信对单用户高并发推送时需异步入库 + 批量写。
- 已读/未读状态写入要走更新而非删除。
- 客服消息需要会话上下文聚合，不要按单条独立返回。
