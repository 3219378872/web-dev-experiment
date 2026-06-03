# Context: Seata Integration

## Objective
按 `docs/superpowers/specs/2026-06-02-infra-integration-design.md` 的 Seata 子系统要求，用 Seata AT 模式为下单（`OrderServiceImpl.createOrder`）和余额支付（`PayOrderServiceImpl.tryPayOrderByBalance`）两条跨服务链路提供分布式事务一致性，并通过 branch→PR→CI/review 门控。

## Scope
- In scope: seata-server compose 容器、`undo_log` 表、trade/item/pay/user 四服务接入 `seata-spring-boot-starter`（默认关）、两处 `@GlobalTransactional`、扣减方法事务化、Seata AT 回滚集成测试、KB 同步。
- Out of scope: TCC 模式、生产级 Seata HA 集群、cart-service（其副作用已 MQ 异步化，不入全局事务）、MinIO（PR #45）、RabbitMQ（PR #46）。

## Related Artifacts
- Spec: `docs/superpowers/specs/2026-06-02-infra-integration-design.md`（§4 共享基础设施、§7 Seata AT）
- Plan: `docs/superpowers/plans/2026-06-03-seata-integration.md`

## Likely Files
- `pom.xml`、`trade-service/pom.xml`、`item-service/pom.xml`、`pay-service/pom.xml`、`user-service/pom.xml`
- `trade-service/src/main/resources/application.yaml`（item/pay/user 同名文件同改）
- `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java`
- `pay-service/src/main/java/com/hmall/service/impl/PayOrderServiceImpl.java`
- `item-service/src/main/java/com/hmall/item/service/impl/ItemServiceImpl.java`
- `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`
- `docs/sql/init-all-tables.sql`、`docker-compose.yml`
- `trade-service/src/test/java/com/hmall/it/**`（新增 SeataOrderRollbackIT）
- `docs/knowledge-base/modules/trade-service.md`、`docs/knowledge-base/flows/order-checkout-flow.md`

## Runtime Evidence To Inspect First
- 所有服务连同一个 `hmall` 库（compose `MYSQL_DATABASE: hmall`）：spec 说"5 库各一张 undo_log"，实际单库只需一张。
- RabbitMQ 已把清车/改单状态异步化；`hm-common/.../outbox/RabbitMqMessagePublisher.publish` 已在事务活跃时延迟到 `afterCommit`，满足 spec §7 的 MQ 协同——本任务不改 MQ 代码。
- 真实全局事务参与方收敛为：createOrder=trade(TM)+item(RM)，tryPay=pay(TM)+user(RM)。cart/trade(在支付流) 因异步副作用排除。
- `deductStock`/`deductMoney` 当前非 `@Transactional`，需补以保证 RM 分支边界。
- spring-cloud-alibaba 2021.0.4.0 默认带旧版 seata 客户端，需对齐到 server 的 1.8.0。

## Safety Constraints
- 不在 compose/application.yaml 硬编码真实密钥；Seata/Nacos 凭据走 env 默认值。
- 不破坏 `hm-common.domain.R`/`PageDTO` 响应封装与既有 endpoint 形态。
- 单元测试不依赖 Seata Server：`seata.enabled` 默认 `false`，`test` job 不变重。
- 触碰 Seata 事务前必须有本任务记录（已建）。

## Worktree Or Branch
- `task/2026-06-03-seata-integration`

## Open Questions
- none
