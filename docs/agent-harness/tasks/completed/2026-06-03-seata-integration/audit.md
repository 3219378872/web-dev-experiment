# Audit

| Requirement (spec §4/§7/§8) | Status | Evidence |
| --- | --- | --- |
| 触碰 Seata 前先建 harness 任务（Safety 硬约束） | done | 本任务记录在 `createOrder`/`tryPay` 改动前建立，`task.yaml` status active。 |
| seata-server 容器（Nacos 注册、AT、健康检查） | done | `docker-compose.yml` 新增 `seataio/seata-server:1.8.0`，Nacos 注册、`STORE_MODE=file`、`curl :7091/health` 健康检查，depends_on nacos+mysql。 |
| 各业务库建 undo_log（SQL 非 JPA DDL） | done | `docs/sql/init-all-tables.sql` 加 `undo_log`。**偏差**：本仓库所有服务共用单 `hmall` 库（compose `MYSQL_DATABASE: hmall`），故只需一张 undo_log，而非 spec 设想的 5 库各一张。 |
| 业务服务引入 spring-cloud-starter-alibaba-seata | done | trade/item/pay/user 四服务 pom 引入，排除 alibaba 自带旧版、对齐 `io.seata:seata-spring-boot-starter:1.8.0`（root pom dependencyManagement 锁定）。**偏差**：cart-service 未接入——其下单副作用（清车）已 RabbitMQ 异步化，不入全局事务，符合 spec §7「把已异步化的副作用从全局事务里摘出去」。 |
| 配置 tx-service-group / vgroup-mapping | done | 四服务 `application.yaml`：`tx-service-group=hmall-tx-group`、`vgroup-mapping.hmall-tx-group=default`、`grouplist.default=${hm.seata.host:127.0.0.1}:8091`、`data-source-proxy-mode=AT`。 |
| DataSourceProxy 由 starter 自动代理 | done | SeataOrderRollbackIT 日志 `branch register success ... lockKeys:order:...` 证明 AT 数据源代理生效。 |
| createOrder 加 @GlobalTransactional | done | `trade-service/.../OrderServiceImpl.createOrder` 标 `@GlobalTransactional(name="createOrder", rollbackFor=Exception.class)` + `@Transactional`。 |
| tryPayOrderByBalance 加 @GlobalTransactional | done | `pay-service/.../PayOrderServiceImpl.tryPayOrderByBalance` 标 `@GlobalTransactional(name="tryPayByBalance")` + `@Transactional`。 |
| RM 分支边界完整 | done | `ItemServiceImpl.deductStock`、`UserServiceImpl.deductMoney` 加 `@Transactional`，作为 item/user 的 AT RM 分支。 |
| 与 RabbitMQ 协同：MQ 发送在事务提交后、落在全局事务外 | done | 既有 `hm-common/.../outbox/RabbitMqMessagePublisher.publish` 已在事务活跃时 `registerSynchronization` 到 `afterCommit`，无需改动；本任务不动 MQ 代码。 |
| 单测不依赖 Seata Server（seata.enabled=false） | done | 四服务 `seata.enabled=${SEATA_ENABLED:false}`；`mvn test` 全过，`test` job 不变重。 |
| 集成测试覆盖核心回滚场景 | done（范围说明） | `SeataOrderRollbackIT`（Testcontainers MySQL+seata-server）验证全局提交落库、全局回滚撤销。跨服务 RM（item/user）回滚因 Feign 在单模块测试中被 mock 无法在此覆盖，由 docker-compose 手测/smoke 验证。 |
| 冒烟：下单成功路径 | deferred | 现有 `scripts/smoke/` 覆盖只读链路；下单端到端 + Seata 回滚的 compose 手测列入 handoff 后续。 |
| 对应 KB 页更新（K005） | done | trade/pay/item/user 模块页 + order-checkout-flow + auth-and-gateway-flow 已同步，`knowledge_base.py check --base origin/main` 通过。 |
| harness/KB/engineering-lint 全过 | done | 见 verification.md，三者均 Exit 0。 |
| branch→PR→CI→review→合并→删分支 | in progress | 分支 `task/2026-06-03-seata-integration`，PR 待开。 |
