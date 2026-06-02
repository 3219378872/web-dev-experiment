package com.hmall.common.mq.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
public class RabbitMqMessagePublisher implements MqMessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectMapper objectMapper;

    public RabbitMqMessagePublisher(
            RabbitTemplate rabbitTemplate,
            ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.objectMapper = objectMapper;
        this.rabbitTemplate.setConfirmCallback(this::handleConfirm);
        this.rabbitTemplate.setReturnsCallback(this::handleReturn);
    }

    @Override
    public void publish(String exchange, String routingKey, Object payload) {
        OutboxCorrelationData correlationData = new OutboxCorrelationData(exchange, routingKey, payload);
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload, correlationData);
        } catch (RuntimeException e) {
            saveOutbox(exchange, routingKey, payload, e);
            log.warn("RabbitMQ publish failed; saved outbox fallback: exchange={}, routingKey={}",
                    exchange, routingKey, e);
        }
    }

    private void handleConfirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack || !(correlationData instanceof OutboxCorrelationData)) {
            return;
        }
        OutboxCorrelationData outbox = (OutboxCorrelationData) correlationData;
        saveOutbox(outbox.exchange, outbox.routingKey, outbox.payload, cause);
    }

    private void handleReturn(ReturnedMessage returnedMessage) {
        String payload = new String(returnedMessage.getMessage().getBody(), StandardCharsets.UTF_8);
        saveSerializedOutbox(
                returnedMessage.getExchange(),
                returnedMessage.getRoutingKey(),
                payload,
                "returned:" + returnedMessage.getReplyCode() + ":" + returnedMessage.getReplyText());
    }

    private void saveOutbox(String exchange, String routingKey, Object payload, Object failure) {
        try {
            saveSerializedOutbox(exchange, routingKey, objectMapper.writeValueAsString(payload), failure);
        } catch (Exception serializationFailure) {
            log.warn("Failed to serialize RabbitMQ outbox payload: exchange={}, routingKey={}",
                    exchange, routingKey, serializationFailure);
        }
    }

    private void saveSerializedOutbox(String exchange, String routingKey, String payload, Object failure) {
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
                    payload,
                    "FAILED",
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now());
        } catch (Exception outboxFailure) {
            log.warn("Failed to persist RabbitMQ outbox message: exchange={}, routingKey={}",
                    exchange, routingKey, outboxFailure);
        }
    }

    private static class OutboxCorrelationData extends CorrelationData {
        private final String exchange;
        private final String routingKey;
        private final Object payload;

        private OutboxCorrelationData(String exchange, String routingKey, Object payload) {
            super(UUID.randomUUID().toString());
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.payload = payload;
        }
    }
}
