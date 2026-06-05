package com.hmall;

import com.hmall.api.client.ItemClient;
import com.hmall.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
    "spring.cloud.bootstrap.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.cloud.openfeign.FeignAutoConfiguration,org.springframework.cloud.openfeign.FeignClientsConfiguration"
})
@Transactional
public abstract class UserServiceTestBase {

    @MockBean
    protected RedisTemplate<String, Object> redisTemplate;

    @MockBean
    protected org.springframework.mail.javamail.JavaMailSender mailSender;

    @MockBean
    protected ItemClient itemClient;

    @Autowired
    protected org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    protected static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUpContext() {
        UserContext.setUser(TEST_USER_ID);
    }

    @AfterEach
    void tearDownContext() {
        UserContext.removeUser();
    }

    /**
     * 准备 Redis ValueOperations mock，用于 stub redisTemplate.opsForValue()
     */
    @SuppressWarnings("unchecked")
    protected ValueOperations<String, Object> prepareValueOps() {
        ValueOperations<String, Object> ops = org.mockito.Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        return ops;
    }
}
