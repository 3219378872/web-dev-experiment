---
title: notify-service
tracks:
  - notify-service/
last_synced_commit: 93d535b
last_synced_date: 2026-06-06
sync_note: "add images field to Feedback entity and schema for issue #106 feedback image upload"
---

# notify-service

## 职责

站内通知、客服消息、用户反馈。后端各业务事件（下单成功/发货/退款/系统公告）
最终在这里落库，前端拉取展示。

## 公开接口与契约

- HTTP API：`/notifications/**`、`/messages/**`、`/feedbacks/**`、`/faqs`。
- 持久层：`hm-notify` 数据库；表 `notifications`、`customer_messages`、`feedbacks`、`faq`。
- FAQ 端点：
  - `GET /faqs` → `List<FaqVO>`：返回启用状态（status=1）的 FAQ 列表，按 sort 升序 + createTime 降序。
- 客服消息端点（占位，前端轮询消费）：
  - `POST /messages` → `R<Void>`：用户发送消息。
  - `GET /admin/messages` → `PageDTO<CustomerMessage>`：管理端消息列表。
  - `PUT /admin/messages/{id}/reply` → `R<Void>`：管理端回复。
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
  `controller/CustomerMessageController.java`、`controller/FeedbackController.java`、
  `controller/FaqController.java`。
- `service/INotificationService.java` / `IFaqService.java` 与 impl。
- `mapper/NotificationMapper.java` / `CustomerMessageMapper.java` / `FeedbackMapper.java` /
  `FaqMapper.java`。
- `domain/po/Faq.java`、`domain/vo/FaqVO.java`。
- `mq/OrderEventListener.java` —— 订单创建与状态事件消费入口。

## 注意事项与陷阱

- 站内信对单用户高并发推送时需异步入库 + 批量写。
- 已读/未读状态写入要走更新而非删除。
- 客服消息需要会话上下文聚合，不要按单条独立返回。
- MQ listener 保存的是 `CustomerMessage`；新增订单事件类型时要同时补充文案映射与测试。
- `OrderEventListener` 失败时通过 [hm-common](hm-common.md) `MqConsumerSupport`
  先延迟重试，超过上限后进入死信队列。
