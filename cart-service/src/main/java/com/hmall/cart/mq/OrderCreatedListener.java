package com.hmall.cart.mq;

import com.hmall.cart.service.ICartService;
import com.hmall.common.mq.MqConstants;
import com.hmall.common.mq.event.OrderCreatedEvent;
import com.hmall.common.utils.UserContext;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final ICartService cartService;

    @RabbitListener(queues = MqConstants.CART_ORDER_CREATE_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event, Message message, Channel channel) throws Exception {
        try {
            handleOrderCreated(event);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            throw e;
        }
    }

    public void handleOrderCreated(OrderCreatedEvent event) {
        if (event == null || event.getUserId() == null || event.getItemIds() == null || event.getItemIds().isEmpty()) {
            return;
        }
        Long previousUser = UserContext.getUser();
        try {
            UserContext.setUser(event.getUserId());
            cartService.removeByItemIds(event.getItemIds());
        } finally {
            if (previousUser == null) {
                UserContext.clear();
            } else {
                UserContext.setUser(previousUser);
            }
        }
    }
}
