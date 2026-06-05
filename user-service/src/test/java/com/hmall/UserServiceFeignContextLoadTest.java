package com.hmall;

import com.hmall.api.client.ItemClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 不 Mock Feign 客户端的上下文加载测试。
 * 目的：在启动期真实装配 Feign 子上下文（含 feignLoggerFactory → Slf4jLogger），
 * 防止运行时缺失 feign-slf4j 等依赖的启动崩溃问题在测试中被 @MockBean 掩盖。
 *
 * @see <a href="https://github.com/3219378872/web-dev-experiment/issues/86">Issue #86</a>
 */
@SpringBootTest(properties = {
    "spring.cloud.bootstrap.enabled=false",
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.server-addr=127.0.0.1:18848",
    "spring.main.allow-bean-definition-overriding=true"
})
@Import(FeignAutoConfiguration.class)
@MockBean(RedisTemplate.class)
@MockBean(JavaMailSender.class)
class UserServiceFeignContextLoadTest {

    @Autowired
    private ItemClient itemClient;

    @Test
    void contextLoads_withRealFeignClient() {
        assertThat(itemClient).isNotNull();
    }
}
