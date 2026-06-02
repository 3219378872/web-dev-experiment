package com.hmall.notify.mq;

import com.hmall.common.mq.MqConstants;
import com.hmall.common.mq.event.OrderCreatedEvent;
import com.hmall.common.mq.event.OrderStatusChangedEvent;
import com.hmall.notify.domain.po.CustomerMessage;
import com.hmall.notify.service.ICustomerMessageService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private static final Map<String, String> STATUS_TEXT = Map.of(
            "shipped", "已发货",
            "refund", "已申请退款",
            "cancel", "已取消");

    private final ICustomerMessageService messageService;

    @RabbitListener(queues = MqConstants.NOTIFY_ORDER_CREATE_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event, Message message, Channel channel) throws Exception {
        try {
            handleOrderCreated(event);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            throw e;
        }
    }

    @RabbitListener(queues = MqConstants.NOTIFY_ORDER_STATUS_QUEUE)
    public void onOrderStatusChanged(OrderStatusChangedEvent event, Message message, Channel channel) throws Exception {
        try {
            handleOrderStatusChanged(event);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            throw e;
        }
    }

    public void handleOrderCreated(OrderCreatedEvent event) {
        if (event == null || event.getUserId() == null || event.getOrderId() == null) {
            return;
        }
        saveMessage(event.getUserId(), "订单 " + event.getOrderId() + " 下单成功");
    }

    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event == null || event.getUserId() == null || event.getOrderId() == null) {
            return;
        }
        String text = STATUS_TEXT.getOrDefault(event.getStatus(), "状态已更新");
        saveMessage(event.getUserId(), "订单 " + event.getOrderId() + " " + text);
    }

    private void saveMessage(Long userId, String content) {
        CustomerMessage message = new CustomerMessage();
        message.setUserId(userId);
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(LocalDateTime.now());
        messageService.save(message);
    }
}
