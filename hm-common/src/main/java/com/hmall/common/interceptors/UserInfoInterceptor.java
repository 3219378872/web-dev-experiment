package com.hmall.common.interceptors;

import cn.hutool.core.util.StrUtil;
import com.hmall.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userIdInString = request.getHeader("user-info");
        if (Objects.nonNull(userIdInString) && !userIdInString.isEmpty() && !userIdInString.equals("null")) {
            UserContext.setUser(Long.valueOf(userIdInString));
        }
        String roleInfo = request.getHeader("role-info");
        if (StrUtil.isNotBlank(roleInfo)) {
            UserContext.setRole(roleInfo);
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
