package com.hmall.service.impl;

import com.hmall.TradeServiceTestBase;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.api.dto.OrderFormDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.mq.MqConstants;
import com.hmall.common.mq.event.OrderCreatedEvent;
import com.hmall.common.mq.event.OrderStatusChangedEvent;
import com.hmall.domain.po.Order;
import com.hmall.domain.po.OrderLogistics;
import com.hmall.service.IOrderLogisticsService;
import com.hmall.service.IOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
class OrderServiceImplTest extends TradeServiceTestBase {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private OrderServiceImpl orderServiceImpl;

    @Autowired
    private IOrderLogisticsService logisticsService;

    @Test
    void createOrder_validOrder_success() {
        ItemDTO item = new ItemDTO();
        item.setId(100L);
        item.setPrice(5000);
        item.setName("测试商品");
        item.setSpec("标准");
        item.setImage("/img/test.png");
        when(itemClient.queryItemByIds(anySet())).thenReturn(List.of(item));

        OrderFormDTO form = new OrderFormDTO();
        form.setPaymentType(1);
        OrderDetailDTO detail = new OrderDetailDTO();
        detail.setItemId(100L);
        detail.setNum(2);
        form.setDetails(List.of(detail));

        Long orderId = orderService.createOrder(form);

        assertThat(orderId).isNotNull();
        Order order = orderService.getById(orderId);
        assertThat(order.getTotalFee()).isEqualTo(10000);
        assertThat(order.getStatus()).isEqualTo(1);
        verify(itemClient).deductStock(any());
        verify(cartClient, never()).deleteCartItemByIds(any());
        runAfterCommitCallbacks();
        verify(rabbitTemplate).convertAndSend(
                eq(MqConstants.TRADE_EXCHANGE),
                eq(MqConstants.ORDER_CREATE_KEY),
                isA(OrderCreatedEvent.class),
                any(CorrelationData.class));
        verify(rabbitTemplate).convertAndSend(
                eq(MqConstants.DELAY_EXCHANGE),
                eq(MqConstants.ORDER_DELAY_KEY),
                isA(OrderCreatedEvent.class),
                any(CorrelationData.class));
    }

    @Test
    void createOrder_itemNotExist_throwsBadRequest() {
        when(itemClient.queryItemByIds(anySet())).thenReturn(Collections.emptyList());

        OrderFormDTO form = new OrderFormDTO();
        form.setPaymentType(1);
        OrderDetailDTO detail = new OrderDetailDTO();
        detail.setItemId(100L);
        detail.setNum(1);
        form.setDetails(List.of(detail));

        assertThatThrownBy(() -> orderService.createOrder(form))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("商品不存在");
    }

    @Test
    void markOrderPaySuccess_updatesStatus() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(1);
        orderService.save(order);
        Long orderId = order.getId();

        orderService.markOrderPaySuccess(orderId);

        Order updated = orderService.getById(orderId);
        assertThat(updated.getStatus()).isEqualTo(2);
        assertThat(updated.getPayTime()).isNotNull();
    }

    @Test
    void handlePaySuccess_closedOrder_ignoresDuplicateOrStaleMessage() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(5);
        orderService.save(order);

        orderServiceImpl.handlePaySuccess(new com.hmall.common.mq.event.PaySuccessEvent(
                10L, order.getId(), 1L, java.time.LocalDateTime.now()));

        Order updated = orderService.getById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(5);
        assertThat(updated.getPayTime()).isNull();
    }

    @Test
    void handleOrderClose_pendingOrder_closes() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(1);
        orderService.save(order);

        orderServiceImpl.handleOrderClose(new OrderCreatedEvent(order.getId(), 1L, List.of(100L)));

        Order updated = orderService.getById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(5);
        assertThat(updated.getCloseTime()).isNotNull();
    }

    @Test
    void handleOrderClose_paidOrder_ignores() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(2);
        orderService.save(order);

        orderServiceImpl.handleOrderClose(new OrderCreatedEvent(order.getId(), 1L, List.of(100L)));

        Order updated = orderService.getById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(2);
        assertThat(updated.getCloseTime()).isNull();
    }

    @Test
    void cancelOrder_pendingOrder_cancels() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(1);
        orderService.save(order);
        Long orderId = order.getId();

        orderService.cancelOrder(orderId, 1L);

        Order updated = orderService.getById(orderId);
        assertThat(updated.getStatus()).isEqualTo(5);
        assertThat(updated.getCloseTime()).isNotNull();
        runAfterCommitCallbacks();
        verify(rabbitTemplate).convertAndSend(
                eq(MqConstants.TRADE_EXCHANGE),
                eq(MqConstants.orderStatusKey("cancel")),
                isA(OrderStatusChangedEvent.class),
                any(CorrelationData.class));
    }

    @Test
    void cancelOrder_wrongUser_throwsBadRequest() {
        Order order = new Order();
        order.setUserId(999L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(1);
        orderService.save(order);
        Long orderId = order.getId();

        assertThatThrownBy(() -> orderService.cancelOrder(orderId, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    void cancelOrder_nonPending_throwsBizIllegal() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(2);
        orderService.save(order);
        Long orderId = order.getId();

        assertThatThrownBy(() -> orderService.cancelOrder(orderId, 1L))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("不可取消");
    }

    @Test
    void confirmReceive_shippedOrder_confirms() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(3);
        orderService.save(order);
        Long orderId = order.getId();

        orderService.confirmReceive(orderId, 1L);

        Order updated = orderService.getById(orderId);
        assertThat(updated.getStatus()).isEqualTo(4);
        assertThat(updated.getEndTime()).isNotNull();
    }

    @Test
    void confirmReceive_notShipped_throwsBizIllegal() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(1);
        orderService.save(order);
        Long orderId = order.getId();

        assertThatThrownBy(() -> orderService.confirmReceive(orderId, 1L))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("不可确认收货");
    }

    @Test
    void refund_paidOrder_refunds() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(2);
        orderService.save(order);
        Long orderId = order.getId();

        orderService.refund(orderId, 1L);

        Order updated = orderService.getById(orderId);
        assertThat(updated.getStatus()).isEqualTo(6);
        runAfterCommitCallbacks();
        verify(rabbitTemplate).convertAndSend(
                eq(MqConstants.TRADE_EXCHANGE),
                eq(MqConstants.orderStatusKey("refund")),
                isA(OrderStatusChangedEvent.class),
                any(CorrelationData.class));
    }

    @Test
    void refund_pendingOrder_throwsBizIllegal() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(1);
        orderService.save(order);
        Long orderId = order.getId();

        assertThatThrownBy(() -> orderService.refund(orderId, 1L))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("不可申请退款");
    }

    @Test
    void ship_existingOrder_ships() {
        Order order = new Order();
        order.setUserId(1L);
        order.setTotalFee(10000);
        order.setPaymentType(1);
        order.setStatus(1);
        orderService.save(order);
        Long orderId = order.getId();

        OrderLogistics logistics = new OrderLogistics();
        logistics.setOrderId(orderId);
        logisticsService.save(logistics);

        orderService.ship(orderId, "SF12345678");

        Order updatedOrder = orderService.getById(orderId);
        assertThat(updatedOrder.getStatus()).isEqualTo(3);
        assertThat(updatedOrder.getConsignTime()).isNotNull();

        OrderLogistics updatedLogistics = logisticsService.lambdaQuery()
                .eq(OrderLogistics::getOrderId, orderId)
                .one();
        assertThat(updatedLogistics).isNotNull();
        assertThat(updatedLogistics.getLogisticsNumber()).isEqualTo("SF12345678");
        runAfterCommitCallbacks();
        verify(rabbitTemplate).convertAndSend(
                eq(MqConstants.TRADE_EXCHANGE),
                eq(MqConstants.orderStatusKey("shipped")),
                isA(OrderStatusChangedEvent.class),
                any(CorrelationData.class));
    }

    private void runAfterCommitCallbacks() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);
        }
    }
}
