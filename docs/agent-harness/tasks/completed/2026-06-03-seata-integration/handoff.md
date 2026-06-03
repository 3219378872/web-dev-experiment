# Handoff: Seata Integration

## Status
实现完成，本地验收全绿，待开 PR 过 CI/codex-review。分支 `task/2026-06-03-seata-integration`。

## Files Changed
- `docs/superpowers/plans/2026-06-03-seata-integration.md`（新增实施计划）
- `docs/agent-harness/tasks/active/2026-06-03-seata-integration/*`
- `pom.xml`（`<seata.version>1.8.0`，dependencyManagement 锁 `io.seata:seata-spring-boot-starter`）
- `trade-service/pom.xml`、`item-service/pom.xml`、`pay-service/pom.xml`、`user-service/pom.xml`（seata starter）
- `trade-service/pom.xml`（test 加 `org.testcontainers:mysql`）
- 四服务 main `application.yaml`（`seata.*` 配置块，默认 `seata.enabled=false`）：
  `trade-service/src/main/resources/application.yaml` 等
- `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java`、
  `pay-service/src/main/java/com/hmall/service/impl/PayOrderServiceImpl.java`（`@GlobalTransactional`）
- `item-service/src/main/java/com/hmall/item/service/impl/ItemServiceImpl.java`、
  `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`（扣减加 `@Transactional`）
- `docs/sql/init-all-tables.sql`（`undo_log` 表）
- `docker-compose.yml`（`seata-server` 服务、`hm.seata.host` env、四服务 `SEATA_ENABLED=true` + depends_on）
- `trade-service/src/test/java/com/hmall/it/SeataOrderRollbackIT.java`（Testcontainers AT 回滚 IT）
- `trade-service/src/test/resources/sql/schema.sql`（测试库 `undo_log`，H2/MySQL 便携 DDL）
- KB：`docs/knowledge-base/modules/trade-service.md`、`docs/knowledge-base/modules/pay-service.md`、
  `docs/knowledge-base/modules/item-service.md`、`docs/knowledge-base/modules/user-service.md`、
  `docs/knowledge-base/flows/order-checkout-flow.md`、`docs/knowledge-base/flows/auth-and-gateway-flow.md`

## 设计要点 / 对 spec 的偏差
- **单库单 undo_log**：所有服务连同一 `hmall` 库，故只建一张 `undo_log`（spec 设想 5 库各一张）。
- **参与方收敛**：RabbitMQ 已异步化清车/改单状态，故全局事务真实参与方为
  createOrder=trade(TM)+item(RM)、tryPay=pay(TM)+user(RM)；cart 未接入 Seata。
- **MQ 协同复用既有实现**：`RabbitMqMessagePublisher` 已 afterCommit 发送，满足 spec §7，未改 MQ。
- **配置选型**：server 用 Nacos 注册；客户端 `config.type=file` 把 vgroup-mapping 写进
  `application.yaml`，避免往 Nacos 灌 Seata 配置。IT 用 `registry.type=file` 直连 Testcontainer。

## Known Risks / 后续
- **集成测试范围**：SeataOrderRollbackIT 仅验证单模块全局提交/回滚语义（trade 本地分支）。
  跨服务 RM（item/user）回滚需多服务 + Feign XID 传播，留 docker-compose 手测/smoke 验证。
  建议后续在 `scripts/smoke/` 加「构造扣库存失败 → 断言订单/库存全回滚」的端到端脚本。
- **版本对齐**：seata 客户端固定 1.8.0 与 server 一致；升级 server 镜像需同步 `<seata.version>`。
- **本机集成测试**：JVM 经 `127.0.0.1` 访问 Docker 发布端口在本环境(WSL2)会超时，
  须用 Docker bridge host `172.17.0.1` + `SPRING_DATASOURCE_URL` env；CI 用 service container 经 `127.0.0.1` 正常。
- **CI codex-review**：需仓库 secrets `OPENAI_API_KEY`/`OPENAI_RESPONSES_API_ENDPOINT`；缺失则该 job 阻塞，须在 PR 描述说明。

## Commands Run
见 `verification.md`（compile / unit / full unit / Seata IT / 全量 integration / compose / harness / KB / engineering-lint，均 Exit 0）。

## Worktree Or Branch
- `task/2026-06-03-seata-integration`
