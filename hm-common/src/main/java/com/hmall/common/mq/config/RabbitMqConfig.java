package com.hmall.common.mq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmall.common.mq.MqConstants;
import com.hmall.common.mq.consumer.MqConsumerSupport;
import com.hmall.common.mq.outbox.MqMessagePublisher;
import com.hmall.common.mq.outbox.RabbitMqMessagePublisher;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(prefix = "hm.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMqConfig {

    @Bean
    @ConditionalOnMissingBean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            @Value("${spring.rabbitmq.listener.simple.auto-startup:true}") boolean autoStartup) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(false);
        factory.setAutoStartup(autoStartup);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public MqMessagePublisher mqMessagePublisher(
            RabbitTemplate rabbitTemplate,
            ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            ObjectMapper objectMapper,
            MessageConverter messageConverter) {
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setMandatory(true);
        return new RabbitMqMessagePublisher(rabbitTemplate, jdbcTemplateProvider, objectMapper);
    }

    @Bean
    public Declarables hmallRabbitDeclarables() {
        TopicExchange tradeExchange = ExchangeBuilder.topicExchange(MqConstants.TRADE_EXCHANGE).durable(true).build();
        TopicExchange payExchange = ExchangeBuilder.topicExchange(MqConstants.PAY_EXCHANGE).durable(true).build();
        DirectExchange delayExchange = ExchangeBuilder.directExchange(MqConstants.DELAY_EXCHANGE).durable(true).build();
        DirectExchange deadExchange = ExchangeBuilder.directExchange(MqConstants.DEAD_EXCHANGE).durable(true).build();
        DirectExchange retryExchange = ExchangeBuilder.directExchange(MqConstants.RETRY_EXCHANGE).durable(true).build();

        Queue cartOrderCreateQueue = durableQueue(MqConstants.CART_ORDER_CREATE_QUEUE, MqConstants.CART_ORDER_CREATE_RETRY_QUEUE);
        Queue notifyOrderCreateQueue = durableQueue(MqConstants.NOTIFY_ORDER_CREATE_QUEUE, MqConstants.NOTIFY_ORDER_CREATE_RETRY_QUEUE);
        Queue notifyOrderStatusQueue = durableQueue(MqConstants.NOTIFY_ORDER_STATUS_QUEUE, MqConstants.NOTIFY_ORDER_STATUS_RETRY_QUEUE);
        Queue tradePaySuccessQueue = durableQueue(MqConstants.TRADE_PAY_SUCCESS_QUEUE, MqConstants.TRADE_PAY_SUCCESS_RETRY_QUEUE);
        Queue deadQueue = QueueBuilder.durable(MqConstants.MQ_DEAD_QUEUE).build();
        Queue orderDelayQueue = QueueBuilder.durable(MqConstants.ORDER_DELAY_QUEUE)
                .ttl((int) MqConstants.ORDER_CLOSE_DELAY_MS)
                .deadLetterExchange(MqConstants.DELAY_EXCHANGE)
                .deadLetterRoutingKey(MqConstants.ORDER_CLOSE_KEY)
                .build();
        Queue orderCloseQueue = durableQueue(MqConstants.ORDER_CLOSE_QUEUE, MqConstants.ORDER_CLOSE_RETRY_QUEUE);
        Queue cartOrderCreateRetryQueue = retryQueue(MqConstants.CART_ORDER_CREATE_RETRY_QUEUE, MqConstants.CART_ORDER_CREATE_QUEUE);
        Queue notifyOrderCreateRetryQueue = retryQueue(MqConstants.NOTIFY_ORDER_CREATE_RETRY_QUEUE, MqConstants.NOTIFY_ORDER_CREATE_QUEUE);
        Queue notifyOrderStatusRetryQueue = retryQueue(MqConstants.NOTIFY_ORDER_STATUS_RETRY_QUEUE, MqConstants.NOTIFY_ORDER_STATUS_QUEUE);
        Queue tradePaySuccessRetryQueue = retryQueue(MqConstants.TRADE_PAY_SUCCESS_RETRY_QUEUE, MqConstants.TRADE_PAY_SUCCESS_QUEUE);
        Queue orderCloseRetryQueue = retryQueue(MqConstants.ORDER_CLOSE_RETRY_QUEUE, MqConstants.ORDER_CLOSE_QUEUE);

        return new Declarables(
                tradeExchange,
                payExchange,
                delayExchange,
                deadExchange,
                retryExchange,
                cartOrderCreateQueue,
                notifyOrderCreateQueue,
                notifyOrderStatusQueue,
                tradePaySuccessQueue,
                orderDelayQueue,
                orderCloseQueue,
                cartOrderCreateRetryQueue,
                notifyOrderCreateRetryQueue,
                notifyOrderStatusRetryQueue,
                tradePaySuccessRetryQueue,
                orderCloseRetryQueue,
                deadQueue,
                bind(tradeExchange, cartOrderCreateQueue, MqConstants.ORDER_CREATE_KEY),
                bind(tradeExchange, notifyOrderCreateQueue, MqConstants.ORDER_CREATE_KEY),
                bind(tradeExchange, notifyOrderStatusQueue, "order.status.*"),
                bind(payExchange, tradePaySuccessQueue, MqConstants.PAY_SUCCESS_KEY),
                BindingBuilder.bind(orderDelayQueue).to(delayExchange).with(MqConstants.ORDER_DELAY_KEY),
                BindingBuilder.bind(orderCloseQueue).to(delayExchange).with(MqConstants.ORDER_CLOSE_KEY),
                BindingBuilder.bind(cartOrderCreateRetryQueue).to(retryExchange).with(MqConstants.CART_ORDER_CREATE_RETRY_QUEUE),
                BindingBuilder.bind(notifyOrderCreateRetryQueue).to(retryExchange).with(MqConstants.NOTIFY_ORDER_CREATE_RETRY_QUEUE),
                BindingBuilder.bind(notifyOrderStatusRetryQueue).to(retryExchange).with(MqConstants.NOTIFY_ORDER_STATUS_RETRY_QUEUE),
                BindingBuilder.bind(tradePaySuccessRetryQueue).to(retryExchange).with(MqConstants.TRADE_PAY_SUCCESS_RETRY_QUEUE),
                BindingBuilder.bind(orderCloseRetryQueue).to(retryExchange).with(MqConstants.ORDER_CLOSE_RETRY_QUEUE),
                BindingBuilder.bind(deadQueue).to(deadExchange).with(MqConstants.DEAD_KEY));
    }

    @Bean
    @ConditionalOnMissingBean
    public MqConsumerSupport mqConsumerSupport(RabbitTemplate rabbitTemplate) {
        return new MqConsumerSupport(rabbitTemplate);
    }

    private Queue durableQueue(String name, String retryQueueName) {
        return QueueBuilder.durable(name)
                .deadLetterExchange(MqConstants.RETRY_EXCHANGE)
                .deadLetterRoutingKey(retryQueueName)
                .build();
    }

    private Queue retryQueue(String name, String targetQueueName) {
        return QueueBuilder.durable(name)
                .ttl(MqConstants.CONSUMER_RETRY_DELAY_MS)
                .deadLetterExchange("")
                .deadLetterRoutingKey(targetQueueName)
                .build();
    }

    private Binding bind(TopicExchange exchange, Queue queue, String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}
