# Seata AT Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 用 Seata AT 模式为下单（`OrderServiceImpl.createOrder`）和余额支付（`PayOrderServiceImpl.tryPayOrderByBalance`）两条跨服务链路提供分布式事务一致性，任一参与方失败时自动反向补偿，并保持单元测试不依赖 Seata Server。

**Architecture:** 每个参与全局事务的业务服务引入 `io.seata:seata-spring-boot-starter`（版本对齐 server 1.8.0），由 starter 自动用 `DataSourceProxy` 代理数据源、注册 `GlobalTransactionScanner`，并通过 Feign 传播 XID。两个 TM 入口方法加 `@GlobalTransactional`。Seata Server 以 Nacos 为注册中心、`file` 存储模式跑在 `docker-compose` 中。MQ 副作用通知已由既有 `MqMessagePublisher` 延迟到 `afterCommit`，无需改动，天然落在全局事务之外。

**Tech Stack:** Spring Boot 2.7.12, Spring Cloud Alibaba 2021.0.4.0, Seata 1.8.0 (AT), MyBatis-Plus, MySQL 8, JUnit 5, Mockito, Testcontainers (MySQL + seata-server)。

---

## 关键现状与对 spec 的偏差（实现前必读）

1. **单库架构**：所有服务的 `spring.datasource.url` 都指向同一个 `hmall` 库（compose `MYSQL_DATABASE: hmall`）。spec §7 说"trade/pay/user/item/cart 库各建一张 undo_log"，实际**只需在 `hmall` 库建一张 `undo_log`**。
2. **参与方收敛**：RabbitMQ 已把"清车""改订单状态"异步化（`MqMessagePublisher` 在事务提交后才发消息）。因此：
   - `createOrder` 全局事务真实参与方 = **trade**(TM+RM) + **item**(RM，经 `deductStock`)。清车走 MQ，**cart 不入全局事务**。
   - `tryPayOrderByBalance` 全局事务真实参与方 = **pay**(TM+RM) + **user**(RM，经 `deductMoney`)。改订单状态走 MQ，**trade 不入该事务**。
   - 故只给 **trade / item / pay / user** 4 个服务装 Seata starter（spec §7 列的 cart 因副作用已异步而排除）。
3. **MQ 协同已就绪**：`hm-common/.../outbox/RabbitMqMessagePublisher.publish` 已在 `TransactionSynchronizationManager` 活跃时 `registerSynchronization` 到 `afterCommit`，满足 spec §7"事务提交后再发 MQ"。本计划不改 MQ 代码。
4. **Seata 客户端配置选型**：server 用 Nacos 注册（遵 spec §4）；客户端用 `seata.registry.type=nacos` 发现 server，`vgroup-mapping` 写在 `application.yaml`（`config.type=file`），避免往 Nacos 灌 Seata 配置、减少 CI 摇摆。集成测试用 Testcontainers，覆盖为 `registry.type=file` 直连容器。
5. **单测隔离**：`seata.enabled` 默认 `false`（经 `${SEATA_ENABLED:false}`），现有单测与 `test` job 不连 Seata Server。

## File Structure

- Modify `pom.xml` — `dependencyManagement` 固定 `io.seata:seata-spring-boot-starter:1.8.0`，`<seata.version>` 属性。
- Modify `trade-service/pom.xml`、`item-service/pom.xml`、`pay-service/pom.xml`、`user-service/pom.xml` — 引入 seata starter（排除 alibaba 自带旧版 seata）。
- Modify 四个服务 `src/main/resources/application.yaml` — 加 `seata.*` 配置块（默认关）。
- Modify `trade-service/.../service/impl/OrderServiceImpl.java`、`pay-service/.../service/impl/PayOrderServiceImpl.java` — 加 `@GlobalTransactional`。
- Modify `item-service/.../service/impl/ItemServiceImpl.java`、`user-service/.../service/impl/UserServiceImpl.java` — `deductStock`/`deductMoney` 加 `@Transactional`（保证 RM 分支边界完整）。
- Modify `docs/sql/init-all-tables.sql` — 加 `undo_log` 表（Seata AT 标准 DDL）。
- Modify `docker-compose.yml` — 加 `seata-server:1.8.0` 容器 + 四服务注入 `hm.seata.host`。
- Create `trade-service/src/test/java/com/hmall/it/SeataOrderRollbackIT.java` — Testcontainers（MySQL + seata-server）验证 AT 回滚。
- Modify `trade-service/src/test/resources/sql/schema.sql`（及涉及到的测试 schema）— 加 `undo_log`。
- Modify KB 页面 `docs/knowledge-base/modules/{trade-service,pay-service,item-service,user-service}.md`、`flows/order-checkout-flow.md`。
- Create harness 任务 `docs/agent-harness/tasks/active/2026-06-03-seata-integration/*`。

---

## Tasks

### Task 0: 建分支与 harness 任务记录（Safety 硬约束）

**Files:**
- Create: `docs/agent-harness/tasks/active/2026-06-03-seata-integration/{task.yaml,context.md,verification.md,audit.md,handoff.md}`

- [ ] **Step 1:** 从 `main` 切分支。

```bash
git switch -c task/2026-06-03-seata-integration
python3 scripts/agent_harness.py new seata-integration
```

- [ ] **Step 2:** 填 `task.yaml`：`status: active`、`spec`/`plan` 指向本文件、`task_branch: task/2026-06-03-seata-integration`。填 context/verification/audit/handoff 叙述（参考 rabbitmq 任务）。

- [ ] **Step 3:** `python3 scripts/agent_harness.py check` → Exit 0。

- [ ] **Step 4: Commit** `chore(harness): seata integration task record`。

### Task 1: undo_log 表 + Seata Server compose

**Files:**
- Modify: `docs/sql/init-all-tables.sql`
- Modify: `docker-compose.yml`

- [ ] **Step 1:** 在 `init-all-tables.sql` 末尾追加 Seata AT 标准 `undo_log` DDL（单库一张）：

```sql
-- Seata AT 模式回滚日志表（单 hmall 库，所有 RM 共用）
CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
  `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
  `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context, such as serialization',
  `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
  `log_status`    INT          NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
  `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT = 'AT transaction mode undo table';
```

- [ ] **Step 2:** `docker-compose.yml` 新增 `seata-server` 服务（Nacos 注册、file 存储）：

```yaml
  seata-server:
    image: seataio/seata-server:1.8.0
    restart: unless-stopped
    environment:
      SEATA_IP: seata-server
      SEATA_PORT: "8091"
      STORE_MODE: file
      SEATA_CONFIG_NAME: "file:/root/seata-config/registry"
      registry.type: nacos
      registry.nacos.server-addr: nacos:8848
      registry.nacos.namespace: ""
      registry.nacos.group: SEATA_GROUP
      registry.nacos.application: seata-server
      registry.nacos.username: nacos
      registry.nacos.password: nacos
    ports:
      - "7091:7091"
      - "8091:8091"
    depends_on:
      nacos:
        condition: service_healthy
      mysql:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "curl -sf http://localhost:7091/health || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 20
      start_period: 30s
```

- [ ] **Step 3:** 在 `x-java-env` 锚点加 `hm.seata.host: seata-server`，并给 `trade-service`/`item-service`/`pay-service`/`user-service` 的 `depends_on` 增加 `seata-server: { condition: service_healthy }`。（`cart-service` 用独立 environment 块，不加 seata，保持不入全局事务。）

- [ ] **Step 4:** `docker compose config -q` → Exit 0。

- [ ] **Step 5: Commit** `feat(infra): add seata-server compose service and undo_log table`。

### Task 2: Seata 依赖与版本对齐

**Files:**
- Modify: `pom.xml`
- Modify: `trade-service/pom.xml`, `item-service/pom.xml`, `pay-service/pom.xml`, `user-service/pom.xml`

- [ ] **Step 1:** root `pom.xml` properties 加 `<seata.version>1.8.0</seata.version>`；`dependencyManagement` 加：

```xml
            <dependency>
                <groupId>io.seata</groupId>
                <artifactId>seata-spring-boot-starter</artifactId>
                <version>${seata.version}</version>
            </dependency>
```

- [ ] **Step 2:** 在 trade/item/pay/user 四个服务 pom 各加（排除 alibaba 自带旧 seata，引入对齐版本）：

```xml
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>io.seata</groupId>
                    <artifactId>seata-spring-boot-starter</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>io.seata</groupId>
            <artifactId>seata-spring-boot-starter</artifactId>
        </dependency>
```

- [ ] **Step 3:** `mvn -B -ntp -q -pl trade-service,item-service,pay-service,user-service -am compile` → Exit 0（依赖解析成功）。

- [ ] **Step 4: Commit** `build(seata): add seata-spring-boot-starter aligned to 1.8.0`。

### Task 3: 四服务 Seata 客户端配置（默认关，单测隔离）

**Files:**
- Modify: `trade-service/src/main/resources/application.yaml`, `item-service/...`, `pay-service/...`, `user-service/...`

- [ ] **Step 1:** 在每个服务 `application.yaml` 顶层加（`application-id`/datasource 名按服务替换）：

```yaml
seata:
  enabled: ${SEATA_ENABLED:false}
  application-id: ${spring.application.name}
  tx-service-group: hmall-tx-group
  service:
    vgroup-mapping:
      hmall-tx-group: default
    grouplist:
      default: ${hm.seata.host:127.0.0.1}:8091
  config:
    type: file
  registry:
    type: nacos
    nacos:
      server-addr: ${spring.cloud.nacos.server-addr}
      group: SEATA_GROUP
      application: seata-server
      username: nacos
      password: nacos
  data-source-proxy-mode: AT
```

> 说明：`seata.enabled=false` 时 starter 不装 `DataSourceProxy`/`GlobalTransactionScanner`，`@GlobalTransactional` 退化为普通调用，单测与 `test` job 不受影响。compose 中给四服务注入 `SEATA_ENABLED: "true"`（Task 1 的 env 块同步加）。

- [ ] **Step 2:** 回到 Task 1 的 compose env，给 trade/item/pay/user（以及 `x-java-env` 不含 cart）注入 `SEATA_ENABLED: "true"`。cart-service 的 environment 块不加。

- [ ] **Step 3:** `mvn -B -ntp -q -pl trade-service,item-service,pay-service,user-service -am test -DskipITs` → Exit 0（现有单测在 `seata.enabled=false` 下不变绿）。

- [ ] **Step 4: Commit** `feat(seata): client config with seata disabled by default for tests`。

### Task 4: RM 分支边界 — 扣减方法事务化

**Files:**
- Modify: `item-service/src/main/java/com/hmall/item/service/impl/ItemServiceImpl.java`
- Modify: `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`

- [ ] **Step 1:** `ItemServiceImpl.deductStock` 加 `@Transactional`（import `org.springframework.transaction.annotation.Transactional`）：

```java
    @Override
    @Transactional
    public void deductStock(List<OrderDetailDTO> items) {
```

- [ ] **Step 2:** `UserServiceImpl.deductMoney` 加 `@Transactional`：

```java
    @Override
    @Transactional
    public void deductMoney(String pw, Integer totalFee) {
```

- [ ] **Step 3:** `mvn -B -ntp -q -pl item-service,user-service -am test -DskipITs` → Exit 0（现有单测仍过；`@Transactional` 不改变非事务环境下的行为）。

- [ ] **Step 4: Commit** `feat(seata): make deductStock/deductMoney transactional for AT branches`。

### Task 5: 两处 `@GlobalTransactional` 事务边界

**Files:**
- Modify: `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java`
- Modify: `pay-service/src/main/java/com/hmall/service/impl/PayOrderServiceImpl.java`

- [ ] **Step 1:** `OrderServiceImpl.createOrder` 在既有 `@Transactional` 上叠加 `@GlobalTransactional`（import `io.seata.spring.annotation.GlobalTransactional`）：

```java
    @Override
    @GlobalTransactional(name = "createOrder", rollbackFor = Exception.class)
    @Transactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
```

> MQ 发送已经在 `afterCommit`，落在全局事务之外，无需改动方法体。

- [ ] **Step 2:** `PayOrderServiceImpl.tryPayOrderByBalance` 同样叠加：

```java
    @Override
    @GlobalTransactional(name = "tryPayByBalance", rollbackFor = Exception.class)
    @Transactional
    public void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO) {
```

- [ ] **Step 3:** `mvn -B -ntp -q -pl trade-service,pay-service -am test -DskipITs` → Exit 0（`seata.enabled=false` 下 `@GlobalTransactional` 无副作用，既有 `OrderServiceImplTest`/支付测试仍过）。

- [ ] **Step 4: Commit** `feat(seata): wrap createOrder and tryPayByBalance in @GlobalTransactional`。

### Task 6: Seata AT 回滚集成测试（Testcontainers）

**Files:**
- Create: `trade-service/src/test/java/com/hmall/it/SeataOrderRollbackIT.java`
- Modify: `trade-service/src/test/resources/sql/schema.sql`（加 `undo_log`）

**目标**：单模块内可验证的 AT 语义 = 在真实 seata-server + MySQL 下，`@GlobalTransactional` 方法本地写入后抛异常 → 全局回滚 → 本地写入被 undo_log 撤销。跨服务 RM（item/user）回滚因 Feign 在单模块测试中被 mock，无法在此覆盖，留 smoke/手测验证（见 verification.md）。

- [ ] **Step 1:** 在 `trade-service/src/test/resources/sql/schema.sql` 末尾加 Task 1 的 `undo_log` DDL（H2 不参与本 IT，但保持 schema 完整；本 IT 用 MySQL 容器）。

- [ ] **Step 2:** 写 `SeataOrderRollbackIT`（RED——先跑会因缺实现/容器失败，确认测试真正执行）：

```java
package com.hmall.it;

import com.hmall.api.client.ItemClient;
import com.hmall.domain.po.Order;
import com.hmall.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.cloud.bootstrap.enabled=false",
        "seata.enabled=true",
        "seata.registry.type=file",
        "seata.config.type=file",
        "seata.service.vgroup-mapping.hmall-tx-group=default"
})
@ActiveProfiles("test")
@Testcontainers
class SeataOrderRollbackIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("hmall");

    @Container
    static GenericContainer<?> seata = new GenericContainer<>(DockerImageName.parse("seataio/seata-server:1.8.0"))
            .withExposedPorts(8091, 7091)
            .withEnv("STORE_MODE", "file")
            .withEnv("SEATA_PORT", "8091");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> mysql.getJdbcUrl() + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai");
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("seata.service.grouplist.default",
                () -> seata.getHost() + ":" + seata.getMappedPort(8091));
    }

    @Autowired
    private IOrderService orderService;
    @Autowired
    private RollbackProbe probe;
    @MockBean
    private ItemClient itemClient;

    @Test
    void globalRollback_undoesLocalOrderInsert() {
        long before = orderService.count();
        assertThatThrownBy(() -> probe.saveThenFail())
                .isInstanceOf(RuntimeException.class);
        // AT 全局回滚后，本地插入被 undo_log 撤销
        assertThat(orderService.count()).isEqualTo(before);
    }

    @Component
    static class RollbackProbe {
        private final IOrderService orderService;
        RollbackProbe(IOrderService orderService) { this.orderService = orderService; }

        @GlobalTransactional(rollbackFor = Exception.class)
        @Transactional
        public void saveThenFail() {
            Order order = new Order();
            order.setUserId(1L);
            order.setTotalFee(100);
            order.setPaymentType(1);
            order.setStatus(1);
            orderService.save(order);
            throw new RuntimeException("force global rollback");
        }
    }
}
```

- [ ] **Step 3:** 跑测试，确认它**真正执行并先红**（容器拉起、Seata 装配；若一开始因依赖/装配报错属预期 RED）：

```bash
mvn -B -ntp -q -pl trade-service -am -Pintegration verify -DskipUnitTests=true -Dit.test=SeataOrderRollbackIT -DfailIfNoTests=false
```

- [ ] **Step 4:** 迭代到 GREEN：确认 `undo_log` 表由 `schema.sql`/MySQL 初始化存在、`seata.enabled=true` 装配 `DataSourceProxy`、全局回滚后 `count()` 回到 `before`。

- [ ] **Step 5: Commit** `test(seata): AT global rollback integration test with Testcontainers`。

> **Fallback（若 Testcontainers seata-server 在本机/CI 不稳定）**：将 IT 标 `@Disabled("requires seata-server; verified via docker-compose smoke")` 并在 `handoff.md` + PR 描述写明 waiver，改由 `scripts/smoke/` + docker-compose 手测覆盖回滚路径。waiver 必须显式记录。

### Task 7: 文档同步与最终验收

**Files:**
- Modify: `docs/knowledge-base/modules/{trade-service,pay-service,item-service,user-service}.md`
- Modify: `docs/knowledge-base/flows/order-checkout-flow.md`
- Modify: `docs/agent-harness/tasks/active/2026-06-03-seata-integration/*`

- [ ] **Step 1:** 更新 KB：各 module 页写明 Seata starter、`@GlobalTransactional`/`@Transactional` RM 边界、`undo_log`；flow 页补"下单/支付的全局事务边界 + MQ 在事务外"。bump 受影响页的 `last_synced_commit`。

- [ ] **Step 2:** 填 `verification.md`（命令+证据表）、`audit.md`（spec 需求逐条对账，含 §1 单库/§2 参与方收敛偏差说明）、`handoff.md`（变更清单、任何 waiver）。

- [ ] **Step 3:** 全量验收：

```bash
python3 scripts/agent_harness.py check
python3 scripts/knowledge_base.py check --base origin/main
python3 scripts/engineering-lint.py
mvn -B -ntp -q test
mvn -B -ntp -q -Pintegration verify -DskipUnitTests=true   # 需 docker
docker compose config -q
```

全部 Exit 0（Seata IT 若走 fallback 则记 waiver）。

- [ ] **Step 4:** `python3 scripts/agent_harness.py complete 2026-06-03-seata-integration`，移任务到 `completed/`、`status: done`。

- [ ] **Step 5: Commit** `docs(seata): KB + harness sync for seata integration`，推分支、开 PR、过 CI/codex-review、合并、删远程分支。

---

## Self-Review

- **Spec 覆盖**：§4 seata-server compose→Task1；§7 starter/undo_log/DataSourceProxy→Task1-3；两处 `@GlobalTransactional`→Task5；MQ 协同（AFTER_COMMIT）→既有实现已满足（现状说明 #3）；单测 `seata.enabled=false`→Task3；集成回滚用例→Task6；KB/harness→Task0/Task7；branch→PR 循环→Task7。
- **偏差已登记**：单库单 undo_log（vs spec 5 库）、参与方收敛为 trade/item/pay/user（cart 因异步排除）——均在 audit.md 记录。
- **Placeholder 扫描**：无 TBD；所有代码/DDL/config 均给出完整内容。
- **类型一致**：`hmall-tx-group` / `vgroup-mapping` / `grouplist.default` / `seata.enabled` 在 compose、application.yaml、IT 三处命名一致。
