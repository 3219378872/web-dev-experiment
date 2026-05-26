package com.hmall.common.interceptors;

import com.hmall.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class UserInfoInterceptorTest {

    private final UserInfoInterceptor interceptor = new UserInfoInterceptor();

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    @Test
    @DisplayName("无 user-info / role-info header 时不写入上下文")
    void preHandle_noHeaders_leavesContextUnset() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        boolean ok = interceptor.preHandle(req, new MockHttpServletResponse(), new Object());
        assertThat(ok).isTrue();
        assertThat(UserContext.getUser()).isNull();
        assertThat(UserContext.getRole()).isNull();
    }

    @Test
    @DisplayName("user-info=\"null\" 字符串（gateway 未鉴权时的标记）被忽略")
    void preHandle_userInfoLiteralNull_isIgnored() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("user-info", "null");
        interceptor.preHandle(req, new MockHttpServletResponse(), new Object());
        assertThat(UserContext.getUser()).isNull();
    }

    @Test
    @DisplayName("user-info=空串被忽略")
    void preHandle_userInfoEmpty_isIgnored() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("user-info", "");
        interceptor.preHandle(req, new MockHttpServletResponse(), new Object());
        assertThat(UserContext.getUser()).isNull();
    }

    @Test
    @DisplayName("user-info 数字写入 UserContext.getUser()")
    void preHandle_userInfoNumeric_setsUserContext() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("user-info", "12345");
        interceptor.preHandle(req, new MockHttpServletResponse(), new Object());
        assertThat(UserContext.getUser()).isEqualTo(12345L);
    }

    @Test
    @DisplayName("role-info header 写入 UserContext.getRole()")
    void preHandle_roleInfo_setsRoleContext() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("user-info", "1");
        req.addHeader("role-info", "admin");
        interceptor.preHandle(req, new MockHttpServletResponse(), new Object());
        assertThat(UserContext.getRole()).isEqualTo("admin");
    }

    @Test
    @DisplayName("空 / null role-info 被忽略")
    void preHandle_blankRole_isIgnored() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("role-info", "   ");
        interceptor.preHandle(req, new MockHttpServletResponse(), new Object());
        assertThat(UserContext.getRole()).isNull();
    }

    @Test
    @DisplayName("afterCompletion 必须清空 UserContext，避免 ThreadLocal 泄漏到下一请求")
    void afterCompletion_clearsContext() throws Exception {
        UserContext.setUser(99L);
        UserContext.setRole("user");
        interceptor.afterCompletion(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new Object(),
                null);
        assertThat(UserContext.getUser()).isNull();
        assertThat(UserContext.getRole()).isNull();
    }
}
