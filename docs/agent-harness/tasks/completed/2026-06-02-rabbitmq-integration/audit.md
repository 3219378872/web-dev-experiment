# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| Harness task exists before implementation | done | `docs/agent-harness/tasks/active/2026-06-02-rabbitmq-integration/task.yaml` status active. |
| RabbitMQ broker in compose | done | `docker-compose.yml` adds `rabbitmq:3.13-management` and broker env/dependencies for trade/pay/cart/notify services. |
| Shared topology and event contracts | done | `hm-common/src/main/java/com/hmall/common/mq/**` defines constants, events, Rabbit config, manual ack listener factory, outbox publisher. |
| Producer failure capture | done | `RabbitMqMessagePublisherTest` covers synchronous `convertAndSend` exception, publisher confirm nack, and returned unroutable message outbox inserts. |
| Producer transaction boundary | done | `RabbitMqMessagePublisherTest.publish_insideTransactionSendsOnlyAfterCommit` covers deferring broker publish until transaction `afterCommit`. |
| Consumer retry before dead-letter | done | `MqConsumerSupportTest` covers first failure nack into retry topology and max-retry transfer to `hmall.mq.dead.queue`. |
| Payment success async event | done | `PayOrderServiceImpl` publishes `pay.success`; `OrderServiceImpl` consumes it and updates order status. |
| Payment success idempotency | done | `OrderServiceImpl.markOrderPaySuccess` updates only status `1`; `OrderServiceImplTest.handlePaySuccess_closedOrder_ignoresDuplicateOrStaleMessage` covers stale duplicate events. |
| Order create async side effects | done | `OrderServiceImpl.createOrder` publishes `order.create`; cart and notify listener tests cover cleanup/notification. |
| Delayed close unpaid order | done | `RabbitMqConfig` declares TTL delay queue and `OrderServiceImpl` consumes `order.close.queue`. |
| Unit tests broker-free | done | Targeted unit suite passed with mocked `RabbitTemplate` and listener auto-start disabled in unit-test configs. |
| Integration send/receive covered | done | `RabbitMqOrderEventIT` passed with Testcontainers RabbitMQ. |
| KB synchronized | done | hm-common/trade/pay/cart/notify module pages and order checkout flow updated; `knowledge_base.py check` passed. |
| Full integration verification | done | Full `-Pintegration verify` exited 0 locally on 2026-06-02 using Docker bridge host overrides for MySQL/Redis. |
| CLAUDE verification commands | done | Full unit, full integration, harness, KB, engineering lint, and compose config checks exited 0 locally on 2026-06-02. |
