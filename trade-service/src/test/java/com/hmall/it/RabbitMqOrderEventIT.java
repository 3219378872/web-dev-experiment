package com.hmall.it;

import com.hmall.api.client.ItemClient;
import com.hmall.common.mq.MqConstants;
import com.hmall.common.mq.event.PaySuccessEvent;
import com.hmall.domain.po.Order;
import com.hmall.service.IOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {
        "spring.cloud.bootstrap.enabled=false",
        "spring.rabbitmq.listener.simple.auto-startup=true"
})
@ActiveProfiles("test")
@Testcontainers
class RabbitMqOrderEventIT {

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void rabbitProps(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
        registry.add("spring.datasource.url",
                () -> "jdbc:h2:mem:trade_rabbit_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private IOrderService orderService;

    @MockBean
    private ItemClient itemClient;

    @Test
    void paySuccessMessage_updatesOrderStatusThroughRabbitMq() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(1);
        orderService.save(order);

        rabbitTemplate.convertAndSend(
                MqConstants.PAY_EXCHANGE,
                MqConstants.PAY_SUCCESS_KEY,
                new PaySuccessEvent(3000L, order.getId(), 1L, LocalDateTime.now()));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updated = orderService.getById(order.getId());
            assertThat(updated.getStatus()).isEqualTo(2);
            assertThat(updated.getPayTime()).isNotNull();
        });
    }
}
