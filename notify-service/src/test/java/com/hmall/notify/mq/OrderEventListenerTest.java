package com.hmall.notify.mq;

import com.hmall.common.mq.event.OrderCreatedEvent;
import com.hmall.common.mq.event.OrderStatusChangedEvent;
import com.hmall.notify.domain.po.CustomerMessage;
import com.hmall.notify.service.ICustomerMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})
@ActiveProfiles("test")
@Transactional
class OrderEventListenerTest {

    @Autowired
    private OrderEventListener listener;

    @Autowired
    private ICustomerMessageService messageService;

    @Test
    void handleOrderCreated_savesCustomerMessage() {
        listener.handleOrderCreated(new OrderCreatedEvent(123L, 7L, List.of(100L, 101L)));

        CustomerMessage message = messageService.lambdaQuery().eq(CustomerMessage::getUserId, 7L).one();
        assertThat(message).isNotNull();
        assertThat(message.getContent()).contains("123").contains("下单");
        assertThat(message.getStatus()).isZero();
        assertThat(message.getCreateTime()).isNotNull();
    }

    @Test
    void handleOrderStatusChanged_savesCustomerMessage() {
        listener.handleOrderStatusChanged(new OrderStatusChangedEvent(123L, 7L, "shipped"));

        CustomerMessage message = messageService.lambdaQuery().eq(CustomerMessage::getUserId, 7L).one();
        assertThat(message).isNotNull();
        assertThat(message.getContent()).contains("123").contains("已发货");
        assertThat(message.getStatus()).isZero();
    }
}
