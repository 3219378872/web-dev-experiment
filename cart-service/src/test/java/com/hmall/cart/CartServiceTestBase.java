package com.hmall.cart;

import com.hmall.api.client.ItemClient;
import com.hmall.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public abstract class CartServiceTestBase {

    @MockBean
    protected ItemClient itemClient;

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
