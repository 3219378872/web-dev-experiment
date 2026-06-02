package com.hmall;

import com.hmall.api.client.CartClient;
import com.hmall.api.client.ItemClient;
import com.hmall.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public abstract class TradeServiceTestBase {

    @MockBean
    protected ItemClient itemClient;

    @MockBean
    protected CartClient cartClient;

    @MockBean
    protected RabbitTemplate rabbitTemplate;

    protected static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUpContext() {
        UserContext.setUser(TEST_USER_ID);
    }

    @AfterEach
    void tearDownContext() {
        UserContext.clear();
    }
}
