package com.hmall.api.config;

import com.hmall.common.utils.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultFeignConfigTest {

    private RequestInterceptor interceptor;

    @BeforeEach
    void setUp() {
        DefaultFeignConfig config = new DefaultFeignConfig();
        interceptor = config.userInfoRequestInterceptor();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("apply: 用户已登录时添加 user-info 请求头")
    void apply_userLoggedIn_addsUserInfoHeader() {
        UserContext.setUser(42L);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        Map<String, Collection<String>> headers = template.headers();
        assertThat(headers.get("user-info")).containsExactly("42");
    }

    @Test
    @DisplayName("apply: 用户未登录时不添加 user-info 请求头")
    void apply_noUser_doesNotAddHeader() {
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        Map<String, Collection<String>> headers = template.headers();
        assertThat(headers.get("user-info")).isNull();
    }

    @Test
    @DisplayName("apply: 不同用户添加对应的 user-info")
    void apply_differentUsers_addsCorrectHeaders() {
        UserContext.setUser(1L);
        RequestTemplate t1 = new RequestTemplate();
        interceptor.apply(t1);
        assertThat(t1.headers().get("user-info")).containsExactly("1");

        UserContext.setUser(99L);
        RequestTemplate t2 = new RequestTemplate();
        interceptor.apply(t2);
        assertThat(t2.headers().get("user-info")).containsExactly("99");
    }
}
