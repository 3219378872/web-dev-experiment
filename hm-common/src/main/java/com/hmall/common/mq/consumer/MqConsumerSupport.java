package com.hmall.common.mq.consumer;

import com.hmall.common.mq.MqConstants;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MqConsumerSupport {

    private final RabbitTemplate rabbitTemplate;

    public void reject(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (retryCount(message) >= MqConstants.CONSUMER_MAX_RETRIES) {
            rabbitTemplate.send(MqConstants.DEAD_EXCHANGE, MqConstants.DEAD_KEY, message);
            channel.basicAck(deliveryTag, false);
            return;
        }
        channel.basicNack(deliveryTag, false, false);
    }

    private long retryCount(Message message) {
        List<Map<String, ?>> deaths = message.getMessageProperties().getXDeathHeader();
        if (deaths == null || deaths.isEmpty()) {
            return 0L;
        }
        long retries = 0L;
        for (Map<String, ?> death : deaths) {
            Object queue = death.get("queue");
            if (queue instanceof String && ((String) queue).endsWith(".retry.queue")) {
                retries += asLong(death.get("count"));
            }
        }
        return retries;
    }

    private long asLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}
