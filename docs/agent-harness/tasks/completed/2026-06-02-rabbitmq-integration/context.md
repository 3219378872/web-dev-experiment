# Context: Rabbitmq Integration

## Objective
按 `docs/superpowers/specs/2026-06-02-infra-integration-design.md` 的 RabbitMQ 子系统要求，把订单创建、支付成功、订单状态通知、超时关单副作用接入 RabbitMQ，并通过 branch→PR→CI/review 门控。

## Scope
- In scope: `hm-common` MQ 拓扑/事件/outbox，`trade-service` 订单事件生产与支付/延时消费，`pay-service` 支付成功事件生产，`cart-service` 下单清车消费，`notify-service` 下单/状态通知消费，RabbitMQ compose 配置，单元/集成测试，KB 同步。
- Out of scope: MinIO（已在 PR #45 合并）、Seata AT（后续独立 PR）、生产级 RabbitMQ 集群。

## Related Artifacts
- Spec: `docs/superpowers/specs/2026-06-02-infra-integration-design.md`
- Plan: `docs/superpowers/plans/2026-06-02-rabbitmq-integration.md`

## Likely Files
- `hm-common/src/main/java/com/hmall/common/mq/**`
- `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java`
- `pay-service/src/main/java/com/hmall/service/impl/PayOrderServiceImpl.java`
- `cart-service/src/main/java/com/hmall/cart/mq/**`
- `notify-service/src/main/java/com/hmall/notify/mq/**`
- `docker-compose.yml`
- `docs/sql/init-all-tables.sql`
- `docs/knowledge-base/modules/{hm-common,trade-service,pay-service,cart-service,notify-service}.md`
- `docs/knowledge-base/flows/order-checkout-flow.md`

## Runtime Evidence To Inspect First
- `hm-common/pom.xml` has `spring-amqp` and `spring-rabbit` with provided scope, but current code has no `RabbitTemplate` or `@RabbitListener`.
- `trade-service` currently calls `cartClient.deleteCartItemByIds` synchronously in `OrderServiceImpl.createOrder`.
- `pay-service` currently calls `orderClient.updateById` synchronously in `PayOrderServiceImpl.tryPayOrderByBalance`.
- `docker-compose.yml` currently includes MinIO but no RabbitMQ service.

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.
- Unit tests must not require a live broker; use mocked `RabbitTemplate` except explicit integration tests.
- Keep inventory deduction synchronous in `createOrder`; RabbitMQ handles side effects, not core stock consistency.

## Worktree Or Branch
- `task/2026-06-02-rabbitmq-integration`

## Open Questions
- none
