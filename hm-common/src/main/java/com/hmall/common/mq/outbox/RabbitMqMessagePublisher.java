package com.hmall.common.mq.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class RabbitMqMessagePublisher implements MqMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(String exchange, String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
        } catch (RuntimeException e) {
            saveOutbox(exchange, routingKey, payload, e);
            throw e;
        }
    }

    private void saveOutbox(String exchange, String routingKey, Object payload, RuntimeException failure) {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            log.warn("RabbitMQ publish failed and no JdbcTemplate is available for outbox: exchange={}, routingKey={}",
                    exchange, routingKey, failure);
            return;
        }
        try {
            jdbcTemplate.update(
                    "insert into mq_outbox_message(exchange_name, routing_key, payload, status, retry_count, create_time, update_time) values (?, ?, ?, ?, ?, ?, ?)",
                    exchange,
                    routingKey,
                    objectMapper.writeValueAsString(payload),
                    "FAILED",
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now());
        } catch (Exception outboxFailure) {
            log.warn("Failed to persist RabbitMQ outbox message: exchange={}, routingKey={}",
                    exchange, routingKey, outboxFailure);
        }
    }
}
