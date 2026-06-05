package com.hmall.it;

import com.hmall.api.client.OrderClient;
import com.hmall.api.client.UserClient;
import com.hmall.api.dto.OrderDTO;
import com.hmall.api.dto.PayApplyDTO;
import com.hmall.api.dto.PayOrderFormDTO;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.mq.MqConstants;
import com.hmall.common.mq.event.PaySuccessEvent;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.PayOrder;
import com.hmall.enums.PayStatus;
import com.hmall.service.IPayOrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/sql/data-payorder.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class PayOrderServiceImplIT {

    @BeforeEach
    void setUp() { UserContext.setUser(1L); }

    @AfterEach
    void tearDown() { UserContext.removeUser(); }

    @Autowired
    private IPayOrderService payOrderService;

    @MockBean
    private UserClient userClient;

    @MockBean
    private OrderClient orderClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void mockOrder() {
        OrderDTO defaultOrder = new OrderDTO();
        defaultOrder.setId(9999L);
        defaultOrder.setUserId(1L);
        defaultOrder.setStatus(1);
        defaultOrder.setTotalFee(50000);
        when(orderClient.queryOrderById(org.mockito.ArgumentMatchers.anyLong())).thenReturn(defaultOrder);
    }

    @Test
    void applyPayOrder_new_shouldCreatePayOrder() {
        PayApplyDTO dto = PayApplyDTO.builder()
                .bizOrderNo(9999L)
                .amount(50000)
                .payChannelCode("balance")
                .payType(5)
                .orderInfo("test order")
                .build();
        String payOrderId = payOrderService.applyPayOrder(dto);
        assertThat(payOrderId).isNotBlank();

        PayOrder po = payOrderService.getById(Long.valueOf(payOrderId));
        assertThat(po.getBizOrderNo()).isEqualTo(9999L);
        assertThat(po.getStatus()).isEqualTo(PayStatus.WAIT_BUYER_PAY.getValue());
    }

    @Test
    void applyPayOrder_alreadyPaid_shouldThrow() {
        PayApplyDTO dto = PayApplyDTO.builder()
                .bizOrderNo(200002L)
                .amount(30000)
                .payChannelCode("balance")
                .payType(5)
                .orderInfo("test")
                .build();
        assertThatThrownBy(() -> payOrderService.applyPayOrder(dto))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("已经支付");
    }

    @Test
    void tryPayOrderByBalance_shouldSucceed() {
        doNothing().when(userClient).deductMoney(anyString(), anyInt());

        PayOrderFormDTO form = PayOrderFormDTO.builder()
                .id(3000L)
                .pw("admin123")
                .build();
        payOrderService.tryPayOrderByBalance(form);

        PayOrder po = payOrderService.getById(3000L);
        assertThat(po.getStatus()).isEqualTo(PayStatus.TRADE_SUCCESS.getValue());
        assertThat(po.getPaySuccessTime()).isNotNull();
        runAfterCommitCallbacks();
        verify(rabbitTemplate).convertAndSend(
                eq(MqConstants.PAY_EXCHANGE),
                eq(MqConstants.PAY_SUCCESS_KEY),
                isA(PaySuccessEvent.class),
                any(CorrelationData.class));
    }

    @Test
    void tryPayOrderByBalance_wrongStatus_shouldThrow() {
        PayOrderFormDTO form = PayOrderFormDTO.builder()
                .id(3001L)
                .pw("admin123")
                .build();
        assertThatThrownBy(() -> payOrderService.tryPayOrderByBalance(form))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("已支付或关闭");
    }

    private void runAfterCommitCallbacks() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);
        }
    }
}
