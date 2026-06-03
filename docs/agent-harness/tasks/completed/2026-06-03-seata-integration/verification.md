# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `mvn -B -ntp -q -pl trade-service,item-service,pay-service,user-service -am compile`（在线） | pass | Exit 0，2026-06-03；seata-spring-boot-starter 1.8.0 与 spring-cloud-starter-alibaba-seata 依赖解析成功。 |
| `mvn -B -ntp -q -pl trade-service,item-service,pay-service,user-service -am test -DskipITs` | pass | Exit 0，2026-06-03；四服务在 `seata.enabled=false` 下既有单测全过，`@GlobalTransactional`/`@Transactional` 不改变非 Seata 环境行为。 |
| `mvn -B -ntp -q test` | pass | Exit 0，2026-06-03；全量单元测试通过（既有 Nacos 连接 ERROR 噪音存在，Maven 退 0）。 |
| `mvn -B -ntp -pl trade-service -am -Pintegration verify -DskipUnitTests=true -Dit.test=SeataOrderRollbackIT` | pass | Exit 0，2026-06-03；Testcontainers(MySQL 8 + seataio/seata-server:1.8.0)。日志确认 AT：第一次 `branch register success`→`commit status: Committed`（落库 +1），第二次 `will be rollback`→`rollback status: Rollbacked`（撤销）。新增 `createOrder_stockDeductionFailure_rollsBack` 核心验收用例：mock `itemClient.deductStock()` 抛异常，验证真正 `createOrder` 调用触发 `@GlobalTransactional` 回滚，订单未落库。 |
| `SPRING_DATASOURCE_URL=jdbc:mysql://172.17.0.1:3306/hm... SPRING_REDIS_HOST=172.17.0.1 mvn -B -ntp -q -Pintegration verify -DskipUnitTests=true` | pass | Exit 0，2026-06-03；全部 14 个 IT 报告 0 失败，含 SeataOrderRollbackIT、RabbitMqOrderEventIT、OrderServiceImplIT、PayOrderServiceImplIT、ItemServiceImplIT 等。本机 JVM 经 127.0.0.1 访问 Docker 发布端口超时（已知 WSL2 quirk，见 rabbitmq 任务 handoff），改用 Docker bridge host 172.17.0.1；CI 用 127.0.0.1 service container 不受影响。 |
| `docker compose config -q` | pass | Exit 0，2026-06-03；新增 seata-server 服务、四服务 SEATA_ENABLED/depends_on 校验通过。 |
| `python3 scripts/agent_harness.py check` | pass | Exit 0，2026-06-03。 |
| `python3 scripts/knowledge_base.py check --base origin/main` | pass | Exit 0，2026-06-03；K005 共变检查通过（trade/pay/item/user 模块页 + order-checkout-flow + auth-and-gateway-flow 已同步）。 |
| `python3 scripts/engineering-lint.py` | pass | Exit 0，2026-06-03；md 链接、AGENTS↔CLAUDE 镜像、harness、KB 全过。 |
