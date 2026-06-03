package com.hmall;

import com.hmall.api.client.UserClient;
import com.hmall.common.mq.outbox.MqMessagePublisher;
import com.hmall.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})
@Transactional
public abstract class PayServiceTestBase {

    @MockBean
    protected UserClient userClient;

    @MockBean
    protected MqMessagePublisher mqMessagePublisher;

    @MockBean
    protected RabbitTemplate rabbitTemplate;

    @MockBean
    protected RedisConnectionFactory redisConnectionFactory;

    protected static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUpContext() {
        UserContext.setUser(TEST_USER_ID);
    }

    @AfterEach
    void tearDownContext() {
        UserContext.removeUser();
    }
}
