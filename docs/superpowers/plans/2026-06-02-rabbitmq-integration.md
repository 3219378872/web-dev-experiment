# RabbitMQ Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 接入 RabbitMQ，使订单创建、支付成功、订单状态通知和超时关单副作用通过消息异步处理，并保持单测不依赖真实 broker。

**Architecture:** `hm-common` 提供 MQ 拓扑常量、事件 DTO、Rabbit 配置和轻量 outbox 表模型；业务服务只依赖这些公共契约。`trade-service` 负责订单事件发布、支付成功消费和延时关单；`pay-service` 负责支付成功事件发布；`cart-service` 和 `notify-service` 通过监听器消费副作用事件。

**Tech Stack:** Spring Boot 2.7, Spring AMQP/RabbitMQ, MyBatis-Plus, JUnit 5, Mockito, Testcontainers RabbitMQ.

---

## File Structure

- Create `hm-common/src/main/java/com/hmall/common/mq/MqConstants.java` — exchange、routing key、queue、TTL 常量。
- Create `hm-common/src/main/java/com/hmall/common/mq/config/RabbitMqConfig.java` — topic exchange、队列绑定、DLX/TTL、Jackson converter、publisher confirm/return 配置。
- Create `hm-common/src/main/java/com/hmall/common/mq/event/*.java` — `OrderCreatedEvent`、`PaySuccessEvent`、`OrderStatusChangedEvent`。
- Create `hm-common/src/main/java/com/hmall/common/mq/outbox/*` — 轻量 outbox PO/mapper/service，用于发送失败落表。
- Modify service poms — 显式引入 `spring-boot-starter-amqp`，测试按需引入 RabbitMQ Testcontainers。
- Modify `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java` — 发布 `order.create`、`order.delay`、订单状态事件；消费 `pay.success` 和超时关单。
- Modify `pay-service/src/main/java/com/hmall/service/impl/PayOrderServiceImpl.java` — 支付成功后发布 `pay.success`，不再直接 Feign 更新订单。
- Create cart/notify listener classes — 消费订单创建和状态事件。
- Modify schemas and docs — 加 `mq_outbox_message` 表、compose rabbitmq、KB/harness 同步。

## Tasks

### Task 1: RED tests for MQ behavior

**Files:**
- Modify: `trade-service/src/test/java/com/hmall/TradeServiceTestBase.java`
- Modify: `trade-service/src/test/java/com/hmall/service/impl/OrderServiceImplTest.java`
- Modify: `pay-service/src/test/java/com/hmall/it/PayOrderServiceImplIT.java`
- Create: `cart-service/src/test/java/com/hmall/cart/mq/OrderCreatedListenerTest.java`
- Create: `notify-service/src/test/java/com/hmall/notify/mq/OrderEventListenerTest.java`

- [ ] Add `RabbitTemplate` mocks to trade/pay test contexts.
- [ ] Assert `createOrder` publishes `trade.topic/order.create` and does not call `cartClient.deleteCartItemByIds`.
- [ ] Assert `tryPayOrderByBalance` publishes `pay.topic/pay.success` and does not call `orderClient.updateById`.
- [ ] Add listener tests for cart cleanup and notification persistence.
- [ ] Run targeted Maven tests and verify expected compile/test failures before implementation.

### Task 2: Shared RabbitMQ contracts and topology

**Files:**
- Create: `hm-common/src/main/java/com/hmall/common/mq/MqConstants.java`
- Create: `hm-common/src/main/java/com/hmall/common/mq/config/RabbitMqConfig.java`
- Create: `hm-common/src/main/java/com/hmall/common/mq/event/OrderCreatedEvent.java`
- Create: `hm-common/src/main/java/com/hmall/common/mq/event/PaySuccessEvent.java`
- Create: `hm-common/src/main/java/com/hmall/common/mq/event/OrderStatusChangedEvent.java`
- Create: `hm-common/src/main/java/com/hmall/common/mq/outbox/MqOutboxMessage.java`
- Create: `hm-common/src/main/java/com/hmall/common/mq/outbox/MqOutboxMapper.java`
- Create: `hm-common/src/main/java/com/hmall/common/mq/outbox/MqMessagePublisher.java`
- Create: `hm-common/src/main/java/com/hmall/common/mq/outbox/RabbitMqMessagePublisher.java`
- Modify: `hm-common/pom.xml`

- [ ] Add concrete constants for all spec queues/exchanges/routing keys.
- [ ] Declare `trade.topic`, `pay.topic`, `delay.exchange`, business queues, retry/dead-letter queues, and delayed close queue using DLX+TTL.
- [ ] Configure Jackson JSON message conversion, manual ack mode, publisher confirms, and publisher returns.
- [ ] Implement `MqMessagePublisher` to call `RabbitTemplate.convertAndSend`; on exception insert one row into `mq_outbox_message`.

### Task 3: Producers and consumers

**Files:**
- Modify: `trade-service/pom.xml`
- Modify: `pay-service/pom.xml`
- Modify: `cart-service/pom.xml`
- Modify: `notify-service/pom.xml`
- Modify: `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java`
- Modify: `pay-service/src/main/java/com/hmall/service/impl/PayOrderServiceImpl.java`
- Create: `cart-service/src/main/java/com/hmall/cart/mq/OrderCreatedListener.java`
- Create: `notify-service/src/main/java/com/hmall/notify/mq/OrderEventListener.java`

- [ ] Replace sync cart clearing in `createOrder` with `OrderCreatedEvent` publish; keep inventory deduction synchronous.
- [ ] Publish delayed close event on order creation.
- [ ] Replace sync order update in `tryPayOrderByBalance` with `PaySuccessEvent` publish.
- [ ] Consume `pay.success` in trade service by calling idempotent `markOrderPaySuccess`.
- [ ] Publish status events for shipped/refund/cancel paths.
- [ ] Consume status/order events in notify service by saving `CustomerMessage`.

### Task 4: Runtime config, schema, compose, and integration test

**Files:**
- Modify: `docker-compose.yml`
- Modify: `docs/sql/init-all-tables.sql`
- Modify: service `application.yaml` and `application-test.yml` files as needed
- Modify: service test `sql/schema.sql` files as needed
- Create: `trade-service/src/test/java/com/hmall/it/RabbitMqOrderEventIT.java`

- [ ] Add `rabbitmq:3.13-management` service and pass broker env into cart/trade/pay/notify services.
- [ ] Add `mq_outbox_message` table to shared and test schemas.
- [ ] Keep unit tests broker-free with mocked `RabbitTemplate`.
- [ ] Add RabbitMQ Testcontainers integration test proving pay.success send/receive updates an order.

### Task 5: Documentation, harness, and final verification

**Files:**
- Modify: `docs/knowledge-base/modules/hm-common.md`
- Modify: `docs/knowledge-base/modules/trade-service.md`
- Modify: `docs/knowledge-base/modules/pay-service.md`
- Modify: `docs/knowledge-base/modules/cart-service.md`
- Modify: `docs/knowledge-base/modules/notify-service.md`
- Modify: `docs/knowledge-base/flows/order-checkout-flow.md`
- Modify: `docs/agent-harness/tasks/active/2026-06-02-rabbitmq-integration/*.md`

- [ ] Update KB pages for RabbitMQ topology and changed checkout/payment side effects.
- [ ] Run `python3 scripts/agent_harness.py check`.
- [ ] Run `python3 scripts/knowledge_base.py check`.
- [ ] Run `python3 scripts/engineering-lint.py`.
- [ ] Run targeted Maven tests, then `mvn -B -ntp -q test`, `mvn -B -ntp -q -Pintegration verify`, and `docker compose config -q`.
- [ ] Complete harness task, commit, push branch, open PR, and follow CI/codex-review until all required gates pass.
