package com.hmall.common.mq.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RabbitMqMessagePublisherTest {

    private RabbitTemplate rabbitTemplate;
    private JdbcTemplate jdbcTemplate;
    private RabbitMqMessagePublisher publisher;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        ObjectProvider<JdbcTemplate> jdbcTemplateProvider = mock(ObjectProvider.class);
        when(jdbcTemplateProvider.getIfAvailable()).thenReturn(jdbcTemplate);
        publisher = new RabbitMqMessagePublisher(rabbitTemplate, jdbcTemplateProvider, new ObjectMapper());
    }

    @Test
    void publish_whenConvertAndSendThrows_recordsOutboxAndDoesNotRethrow() {
        Map<String, Object> payload = Map.of("orderId", 1L);
        org.mockito.Mockito.doThrow(new AmqpException("broker unavailable"))
                .when(rabbitTemplate)
                .convertAndSend(eq("trade.topic"), eq("order.create"), eq(payload), any(CorrelationData.class));

        assertThatCode(() -> publisher.publish("trade.topic", "order.create", payload))
                .doesNotThrowAnyException();

        verify(jdbcTemplate).update(
                anyString(),
                eq("trade.topic"),
                eq("order.create"),
                eq("{\"orderId\":1}"),
                eq("FAILED"),
                eq(0),
                any(),
                any());
    }

    @Test
    void publishRegistersConfirmCallbackThatRecordsNack() {
        Map<String, Object> payload = Map.of("orderId", 2L);
        ArgumentCaptor<RabbitTemplate.ConfirmCallback> callbackCaptor =
                ArgumentCaptor.forClass(RabbitTemplate.ConfirmCallback.class);
        verify(rabbitTemplate).setConfirmCallback(callbackCaptor.capture());

        publisher.publish("trade.topic", "order.create", payload);
        ArgumentCaptor<CorrelationData> correlationCaptor = ArgumentCaptor.forClass(CorrelationData.class);
        verify(rabbitTemplate).convertAndSend(
                eq("trade.topic"),
                eq("order.create"),
                eq(payload),
                correlationCaptor.capture());

        callbackCaptor.getValue().confirm(correlationCaptor.getValue(), false, "nack");

        verify(jdbcTemplate).update(
                anyString(),
                eq("trade.topic"),
                eq("order.create"),
                eq("{\"orderId\":2}"),
                eq("FAILED"),
                eq(0),
                any(),
                any());
    }

    @Test
    void publishRegistersReturnCallbackThatRecordsReturnedMessageBody() {
        ArgumentCaptor<RabbitTemplate.ReturnsCallback> callbackCaptor =
                ArgumentCaptor.forClass(RabbitTemplate.ReturnsCallback.class);
        verify(rabbitTemplate).setReturnsCallback(callbackCaptor.capture());
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        Message message = new Message("{\"orderId\":3}".getBytes(StandardCharsets.UTF_8), properties);

        callbackCaptor.getValue().returnedMessage(
                new ReturnedMessage(message, 312, "NO_ROUTE", "trade.topic", "order.missing"));

        verify(jdbcTemplate).update(
                anyString(),
                eq("trade.topic"),
                eq("order.missing"),
                eq("{\"orderId\":3}"),
                eq("FAILED"),
                eq(0),
                any(),
                any());
    }
}
