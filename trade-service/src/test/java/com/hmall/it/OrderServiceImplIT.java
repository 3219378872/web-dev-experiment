package com.hmall.it;

import com.hmall.api.client.CartClient;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.api.dto.OrderFormDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Order;
import com.hmall.service.IOrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/sql/data-order.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class OrderServiceImplIT {

    @AfterEach
    void tearDown() { UserContext.removeUser(); }

    @Autowired
    private IOrderService orderService;

    @MockBean
    private ItemClient itemClient;

    @MockBean
    private CartClient cartClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() { UserContext.setUser(1L); }

    @BeforeEach
    void setUpMocks() {
        ItemDTO item = new ItemDTO();
        item.setId(100L);
        item.setName("测试商品");
        item.setPrice(25000);
        item.setSpec("标准");
        item.setImage("/img/test.png");
        when(itemClient.queryItemByIds(anySet())).thenReturn(List.of(item));
        doNothing().when(itemClient).deductStock(anyList());
        doNothing().when(cartClient).deleteCartItemByIds(anySet());
    }

    @Test
    void createOrder_shouldSucceed() {
        OrderFormDTO form = new OrderFormDTO();
        form.setPaymentType(1);
        OrderDetailDTO detail = new OrderDetailDTO();
        detail.setItemId(100L);
        detail.setNum(2);
        form.setDetails(List.of(detail));

        Long orderId = orderService.createOrder(form);
        assertThat(orderId).isPositive();

        Order order = orderService.getById(orderId);
        assertThat(order.getTotalFee()).isEqualTo(50000);
        assertThat(order.getStatus()).isEqualTo(1);
        assertThat(order.getUserId()).isEqualTo(1L);
    }

    @Test
    void cancelOrder_shouldCloseOrder() {
        orderService.cancelOrder(1000L, 1L);
        Order order = orderService.getById(1000L);
        assertThat(order.getStatus()).isEqualTo(5);
        assertThat(order.getCloseTime()).isNotNull();
    }

    @Test
    void cancelOrder_notOwner_shouldThrow() {
        assertThatThrownBy(() -> orderService.cancelOrder(1000L, 999L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("订单不存在");
    }

    @Test
    void markOrderPaySuccess_shouldUpdateStatus() {
        orderService.markOrderPaySuccess(1000L);
        Order order = orderService.getById(1000L);
        assertThat(order.getStatus()).isEqualTo(2);
        assertThat(order.getPayTime()).isNotNull();
    }

    @Test
    void confirmReceive_shouldCompleteOrder() {
        orderService.confirmReceive(1002L, 1L);
        Order order = orderService.getById(1002L);
        assertThat(order.getStatus()).isEqualTo(4);
    }

    @Test
    void refund_paidOrder_shouldRefund() {
        orderService.refund(1001L, 1L);
        Order order = orderService.getById(1001L);
        assertThat(order.getStatus()).isEqualTo(6);
    }

    @Test
    void refund_pendingPayment_shouldThrow() {
        assertThatThrownBy(() -> orderService.refund(1000L, 1L))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("当前状态不可申请退款");
    }
}
