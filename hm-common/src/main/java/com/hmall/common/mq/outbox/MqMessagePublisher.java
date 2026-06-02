package com.hmall.common.mq.outbox;

public interface MqMessagePublisher {
    void publish(String exchange, String routingKey, Object payload);
}
