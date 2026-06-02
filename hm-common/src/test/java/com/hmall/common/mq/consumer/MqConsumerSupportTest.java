package com.hmall.common.mq.consumer;

import com.hmall.common.mq.MqConstants;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MqConsumerSupportTest {

    @Test
    void reject_failedWithoutPriorRetry_nacksToRetryQueue() throws Exception {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        MqConsumerSupport support = new MqConsumerSupport(rabbitTemplate);
        Channel channel = mock(Channel.class);
        Message message = messageWithXDeath(10L, List.of());

        support.reject(message, channel);

        verify(channel).basicNack(10L, false, false);
    }

    @Test
    void reject_afterMaxRetries_sendsToDeadQueueAndAcksOriginal() throws Exception {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        MqConsumerSupport support = new MqConsumerSupport(rabbitTemplate);
        Channel channel = mock(Channel.class);
        Message message = messageWithXDeath(11L, List.of(Map.of(
                "queue", "trade.pay.success.retry.queue",
                "count", 3L)));

        support.reject(message, channel);

        verify(rabbitTemplate).send(eq(MqConstants.DEAD_EXCHANGE), eq(MqConstants.DEAD_KEY), any(Message.class));
        verify(channel).basicAck(11L, false);
    }

    private Message messageWithXDeath(long deliveryTag, List<Map<String, ?>> deaths) {
        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(deliveryTag);
        properties.setHeader("x-death", deaths);
        return new Message("{\"id\":1}".getBytes(), properties);
    }
}
