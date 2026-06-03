package com.hmall.it;

import com.hmall.api.client.ItemClient;
import com.hmall.domain.po.Order;
import com.hmall.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Seata AT 端到端集成测试：在真实 seata-server + MySQL 下，验证 {@code @GlobalTransactional}
 * 方法的全局提交（落库一行）与全局回滚（undo_log 撤销该行）。
 *
 * <p>跨服务 RM（item/user）的回滚因 Feign 在单模块测试中被 mock 无法在此覆盖，
 * 由 docker-compose smoke 与手测验证（见 task 的 verification.md）。
 */
@SpringBootTest(properties = {
        "spring.cloud.bootstrap.enabled=false",
        "seata.enabled=true",
        "seata.registry.type=file",
        "seata.config.type=file",
        "seata.service.vgroup-mapping.hmall-tx-group=default"
})
@ActiveProfiles("test")
@Testcontainers
@Import(SeataOrderRollbackIT.TestConfig.class)
class SeataOrderRollbackIT {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("hmall")
            .withUsername("root")
            .withPassword("root");

    @Container
    static final GenericContainer<?> seata = new GenericContainer<>(DockerImageName.parse("seataio/seata-server:1.8.0"))
            .withExposedPorts(8091, 7091)
            .withEnv("STORE_MODE", "file")
            .withEnv("SEATA_PORT", "8091")
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofMinutes(3));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> mysql.getJdbcUrl() + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false");
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "root");
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
    void globalCommit_persists_then_globalRollback_undoes() {
        long before = orderService.count();

        // 1) 全局事务提交：AT 写 undo_log 后 phase2 清理，落库一行
        probe.saveAndCommit();
        long afterCommit = orderService.count();
        assertThat(afterCommit).isEqualTo(before + 1);

        // 2) 全局事务回滚：抛异常触发 AT 反向补偿，撤销本次插入
        assertThatThrownBy(() -> probe.saveThenFail())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("force global rollback");
        assertThat(orderService.count()).isEqualTo(afterCommit);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        RollbackProbe rollbackProbe(IOrderService orderService) {
            return new RollbackProbe(orderService);
        }
    }

    /** 被 Seata GlobalTransactionScanner 代理的探针 bean。 */
    static class RollbackProbe {
        private final IOrderService orderService;

        RollbackProbe(IOrderService orderService) {
            this.orderService = orderService;
        }

        @GlobalTransactional(rollbackFor = Exception.class)
        @Transactional
        public void saveAndCommit() {
            orderService.save(newOrder());
        }

        @GlobalTransactional(rollbackFor = Exception.class)
        @Transactional
        public void saveThenFail() {
            orderService.save(newOrder());
            throw new RuntimeException("force global rollback");
        }

        private Order newOrder() {
            Order order = new Order();
            order.setUserId(1L);
            order.setTotalFee(100);
            order.setPaymentType(1);
            order.setStatus(1);
            return order;
        }
    }
}
