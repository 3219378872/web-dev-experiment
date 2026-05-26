package com.hmall.common.it;

import com.hmall.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public abstract class AbstractIT {

    protected static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUpUserContext() {
        UserContext.setUser(TEST_USER_ID);
    }

    @AfterEach
    void tearDownUserContext() {
        UserContext.removeUser();
    }
}
